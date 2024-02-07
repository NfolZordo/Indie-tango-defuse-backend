package indie.tango.defuse.services;

import indie.tango.defuse.auth.AuthenticationRequest;
import indie.tango.defuse.auth.AuthenticationResponse;
import indie.tango.defuse.config.JwtService;
import indie.tango.defuse.models.Constants;
import indie.tango.defuse.models.FriendsModel;
import indie.tango.defuse.models.GameSession;
import indie.tango.defuse.repositoriy.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class GameService {
    @Autowired
    private GameSession gameSession;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TaskScheduler taskScheduler;
    private final UserRepository userRepository;
    private final JwtService jwtService;
        private ScheduledFuture<?> timerTask;

    public String createGame(SimpMessageHeaderAccessor headerAccessor, String userName) {
        String gameCode = gameSession.generateGameCode();
        String playerSessionId = headerAccessor.getSessionId();
        gameSession.addPlayerSession(gameCode, playerSessionId, userName);
        gameSession.initStepsCount(gameCode);
        gameSession.initCountsError(gameCode);
        return gameCode;
    }

    public String joinGame(String gameCode, SimpMessageHeaderAccessor headerAccessor, String userName) {
        String playerSessionId = headerAccessor.getSessionId();
        String existingGameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        if (existingGameCode != null) {
            return "Already joined a game";
        }
        if (gameSession.isGameExists(gameCode)) {
            gameSession.addPlayerSession(gameCode, playerSessionId, userName);
            return "Successfully joined the game";
        } else {
            return "Game not found";
        }
    }

    public void startTimer(SimpMessageHeaderAccessor headerAccessor, String gameMode, String token) {
        String playerSessionId = headerAccessor.getSessionId();
        String email = jwtService.extractUsername(token.substring(7));
        gameSession.setGameMod(gameMode);
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.setTimer(gameCode);
        if (!gameSession.getTimerTasks().containsKey(gameCode) || gameSession.getTimerTasks().get(gameCode).isCancelled()) {
            ScheduledFuture<?> timerTask = taskScheduler.scheduleAtFixedRate(() -> {
                int timeLeft = gameSession.decrementTimer(gameCode);
                sendToUsers(gameSession.getPlayerSessions(gameCode), "/queue/getTimerValue", Integer.toString(timeLeft));
                if (timeLeft == 0) {
                    gameSession.stopTimer(gameCode);
                    gameSession.removeOnlinePlayer(email);
                }
            }, 1000);
            gameSession.getTimerTasks().put(gameCode, timerTask);
        }
    }

    public void stopTimer(SimpMessageHeaderAccessor headerAccessor, String token) {
        String email = jwtService.extractUsername(token.substring(7));
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.stopTimer(gameCode);
        gameSession.removeOnlinePlayer(email);

    }

//    public byte[] getTask(String gameCode) throws IOException {
//        char lastChar = gameCode.charAt(gameCode.length() - 1);
//
//        Path filePath = Paths.get("src\\main\\resources\\task\\itd_" + lastChar + ".jpg");
//        byte[] imageBytes = Files.readAllBytes(filePath);
//        return imageBytes;
//    }
public byte[] getTask(String gameCode) throws IOException {
    Integer lastChar = Integer.parseInt(gameCode.substring(gameCode.length() - 1));

    String imageUrl = "https://drive.usercontent.google.com/download?id=" + Constants.imgId.get(lastChar) + "&export=view";
    URL url = new URL(imageUrl);

    Path tempFile = Files.createTempFile("itd_" + lastChar, ".jpg");
    Files.copy(url.openStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

    byte[] imageBytes = Files.readAllBytes(tempFile);
    Files.delete(tempFile);

    return imageBytes;
}

    private <T> void sendToUsers(List<String> playerSessions, String destination, T message) {
        for (String session : playerSessions) {
            SimpMessageHeaderAccessor headerAccessorForUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessorForUser.setSessionId(session);
            headerAccessorForUser.setLeaveMutable(true);
            messagingTemplate.convertAndSendToUser(session, destination, message, headerAccessorForUser.getMessageHeaders());
        }
    }

    public String doStep(SimpMessageHeaderAccessor headerAccessor, String button) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        return gameSession.doStep(gameCode, button);
    }

    public FriendsModel getFriends (String token) {
        String email = jwtService.extractUsername(token.substring(7));
        List<String> allFriends = userRepository.getFriends(email);
        Map<String, String> onlineFriends = gameSession.filterOnlineFriends(allFriends);
        return new FriendsModel(allFriends, onlineFriends);
    }

    @Transactional
    public void addFriends (String token, String friendEmail) {
        String email = jwtService.extractUsername(token.substring(7));

        userRepository.addFriends(userRepository.findByEmail(email).get().getId(), userRepository.findByEmail(friendEmail).get().getId());
    }
}
