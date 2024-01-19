package indie.tango.defuse.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class GameSession {
    private final Map<String, String> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> activeGames = new ConcurrentHashMap<>();
    private final Map<String, Integer> gameTimers = new ConcurrentHashMap<>();
    private Map<String, ScheduledFuture<?>> timerTasks = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TaskScheduler taskScheduler;


    public String generateGameCode() {
        String gameCode = generateRandomCode();
        activeGames.put(gameCode, true); // Позначити гру як активну
        return gameCode;
    }

    public void addPlayerSession(String gameCode, String playerSessionId) {
        playerSessions.put(playerSessionId, gameCode);
    }

    public String getGameCodeForPlayer(String playerSessionId) {
        return playerSessions.get(playerSessionId);
    }

    public List<String> getPlayerSessions(String gameCode) {
        List<String> sessions = new ArrayList<>();
        for (Map.Entry<String, String> entry : playerSessions.entrySet()) {
            if (entry.getValue().equals(gameCode)) {
                sessions.add(entry.getKey());
            }
        }
        return sessions;
    }

    public boolean isGameExists(String gameCode) {
        return activeGames.containsKey(gameCode);
    }

    public void putTimer(String gameCode, int timerValue) {
        gameTimers.put(gameCode, timerValue);
    }
    public int decrementTimer(String gameCode) {
        return gameTimers.compute(gameCode, (key, value) -> value != null && value > 0 ? value - 1 : 0);
    }
    private String generateRandomCode() {
        Random random = new Random();
        int code = 10000 + random.nextInt(90000); // Генерує випадковий код з п'яти цифр
        return String.valueOf(code);
    }

    public void startTimer(Message message, String gameCode, int initialTime) {
        putTimer(gameCode,Integer.parseInt(message.getContent()));
        if (!timerTasks.containsKey(gameCode) || timerTasks.get(gameCode).isCancelled()) {
            ScheduledFuture<?> timerTask = taskScheduler.scheduleAtFixedRate(() -> {
                int timeLeft = decrementTimer(gameCode);
                message.setContent(Integer.toString(timeLeft));
                sendToUsers(gameCode, "/queue/chat", message);

                if (timeLeft == 0) {
                    stopTimer(gameCode);
                }
            }, 1000);

            timerTasks.put(gameCode, timerTask);
        }
    }

    public void stopTimer(String gameCode) {
        ScheduledFuture<?> timerTask = timerTasks.get(gameCode);
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(false);
            timerTasks.remove(gameCode);
        }
    }

    public void sendToUsers(String gameCode, String destination, Message message) {
        List<String> playerSessions = getPlayerSessions(gameCode);
        for (String session : playerSessions) {
            SimpMessageHeaderAccessor headerAccessorForUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessorForUser.setSessionId(session);
            headerAccessorForUser.setLeaveMutable(true);
            messagingTemplate.convertAndSendToUser(session, destination, message, headerAccessorForUser.getMessageHeaders());
        }
    }
}
