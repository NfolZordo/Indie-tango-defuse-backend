package indie.tango.defuse.controllers;

import indie.tango.defuse.models.GameSession;
import indie.tango.defuse.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ScheduledFuture;

@Controller
public class WebSocketController {

    @Autowired
    private GameSession gameSession;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    private ScheduledFuture<?> timerTask;

    @MessageMapping("/createGame")
    @SendToUser("/queue/gameCode")
    public String createGame(SimpMessageHeaderAccessor headerAccessor) {
        String gameCode = gameSession.generateGameCode();
        String playerSessionId = headerAccessor.getSessionId();
        gameSession.addPlayerSession(gameCode, playerSessionId);
        return gameCode;
    }

    @MessageMapping("/joinGame")
    @SendToUser("/queue/joinResult")
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

    @MessageMapping("/sendMessage")
    public void sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        if (gameCode != null) {
            message.setSender(playerSessionId);
            message.setGameCode(gameCode);
            gameSession.sendToUsers(gameCode, "/queue/chat", message);

        }
    }

    @MessageMapping("/startTimer")
    @SendToUser("/queue/startTimer")
    public void startTimer(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
//        gameSession.putTimer(gameCode,Integer.parseInt(message.getContent()));
        gameSession.startTimer(message, gameCode, Integer.parseInt(message.getContent()));
    }
    @MessageMapping("/startTimer2")
    @SendToUser("/queue/startTimer2")
    public void startTimer2(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        gameSession.putTimer(gameCode,Integer.parseInt(message.getContent()));
        // Перевірка, чи таймер вже запущено для даної гри
        if (timerTask == null || timerTask.isCancelled()) {
            // Стартувати таймер, який відсилатиме значення кожну секунду
            timerTask = taskScheduler.scheduleAtFixedRate(() -> {
                int timeLeft = gameSession.decrementTimer(gameCode);
                // Відсилати значення таймеру кожній сесії гравця в грі
                message.setContent(Integer.toString(timeLeft));
                gameSession.sendToUsers(gameCode, "/queue/chat", message);

                // Якщо час вийшов, зупинити таймер
                if (timeLeft == 0) {
                    stopTimer();
                }
            }, 100);
        }
    }

//    private void sendToUser(String gameCode, String destination, Message message) {
//        List<String> playerSessions = gameSession.getPlayerSessions(gameCode);
//        for (String session : playerSessions) {
//            SimpMessageHeaderAccessor headerAccessorForUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
//            headerAccessorForUser.setSessionId(session);
//            headerAccessorForUser.setLeaveMutable(true);
//            messagingTemplate.convertAndSendToUser(session, destination, message, headerAccessorForUser.getMessageHeaders());
//        }
//    }
    @MessageMapping("/stopTimer")
    public void stopTimer() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(false);
            timerTask = null;
        }
    }
}
