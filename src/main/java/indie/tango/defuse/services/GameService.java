package indie.tango.defuse.services;

import indie.tango.defuse.models.GameSession;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
public class GameService {
    @Autowired
    private GameSession gameSession;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TaskScheduler taskScheduler;

    private ScheduledFuture<?> timerTask;

    public String createGame(SimpMessageHeaderAccessor headerAccessor) {
        String gameCode = gameSession.generateGameCode();
        String playerSessionId = headerAccessor.getSessionId();
        gameSession.addPlayerSession(gameCode, playerSessionId);
        gameSession.initStepsCount(gameCode);
        return gameCode;
    }

    public String joinGame(String gameCode, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String existingGameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        if (existingGameCode != null) {
            return "Already joined a game";
        }
        if (gameSession.isGameExists(gameCode)) {
            gameSession.addPlayerSession(gameCode, playerSessionId);
            return "Successfully joined the game";
        } else {
            return "Game not found";
        }
    }

    public void startTimer(SimpMessageHeaderAccessor headerAccessor, String gameMode) {
        String playerSessionId = headerAccessor.getSessionId();
        gameSession.setGameMod(gameMode);
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.setTimer(gameCode);
        if (!gameSession.getTimerTasks().containsKey(gameCode) || gameSession.getTimerTasks().get(gameCode).isCancelled()) {
            ScheduledFuture<?> timerTask = taskScheduler.scheduleAtFixedRate(() -> {
                int timeLeft = gameSession.decrementTimer(gameCode);
                sendToUsers(gameSession.getPlayerSessions(gameCode), "/queue/getTimerValue", Integer.toString(timeLeft));
                if (timeLeft == 0) {
                    gameSession.stopTimer(gameCode);
                }
            }, 1000);
            gameSession.getTimerTasks().put(gameCode, timerTask);
        }
    }

    public void stopTimer(SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.stopTimer(gameCode);
    }

    public byte[] getTask(String gameCode) throws IOException {
        char lastChar = gameCode.charAt(gameCode.length() - 1);

        Path filePath = Paths.get("src\\main\\resources\\task\\itd_" + lastChar + ".jpg");
        byte[] imageBytes = Files.readAllBytes(filePath);
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
}
