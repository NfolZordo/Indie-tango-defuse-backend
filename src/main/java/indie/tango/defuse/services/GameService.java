package indie.tango.defuse.services;

import indie.tango.defuse.models.GameSession;
import indie.tango.defuse.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

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

    public void sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        if (gameCode != null) {
            message.setSender(playerSessionId);
            message.setGameCode(gameCode);
            sendToUsers(gameSession.getPlayerSessions(gameCode), "/queue/chat", message);
        }
    }

    public void startTimer(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.setTimer(gameCode,Integer.parseInt(message.getContent()));
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
//        startTimer(gameCode, Integer.parseInt(message.getContent()));
    }

    public void stopTimer(SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.stopTimer(gameCode);
    }

    public void startTimer(String gameCode, int initialTime) {
        gameSession.setTimer(gameCode,initialTime);
        if (!gameSession.getTimerTasks().containsKey(gameCode) || gameSession.getTimerTasks().get(gameCode).isCancelled()) {
            ScheduledFuture<?> timerTask = taskScheduler.scheduleAtFixedRate(() -> {
                handleTimerTick(gameCode);
                }
            , 1000);
            gameSession.getTimerTasks().put(gameCode, timerTask);
        }
    }

    private void handleTimerTick(String gameCode) {
        int timeLeft = gameSession.decrementTimer(gameCode);
        sendToUsers(gameSession.getPlayerSessions(gameCode), "/queue/getTimerValue", Integer.toString(timeLeft));
        if (timeLeft == 0) {
            gameSession.stopTimer(gameCode);
        }
    }

    private <T> void sendToUsers(List<String> playerSessions, String destination, T message) {
        for (String session : playerSessions) {
            SimpMessageHeaderAccessor headerAccessorForUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessorForUser.setSessionId(session);
            headerAccessorForUser.setLeaveMutable(true);
            messagingTemplate.convertAndSendToUser(session, destination, message, headerAccessorForUser.getMessageHeaders());
        }
    }
}
