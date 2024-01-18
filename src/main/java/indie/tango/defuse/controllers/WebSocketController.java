package indie.tango.defuse.controllers;

import indie.tango.defuse.models.GameSession;
import indie.tango.defuse.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {

    @Autowired
    private GameSession gameSession;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

            // Отримайте всі сесії гравців для даної гри
            List<String> playerSessions = gameSession.getPlayerSessions(gameCode);

            // Відправте повідомлення кожному гравцеві в межах гри
            for (String session : playerSessions) {
                String username = session;  // username - це ім'я користувача, яке ви хочете використовувати
                String destination = "/user/" + username + "/queue/chat";

                SimpMessageHeaderAccessor headerAccessorForUser = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
                headerAccessorForUser.setSessionId(session);
                headerAccessorForUser.setLeaveMutable(true);

                messagingTemplate.convertAndSendToUser(session, "/queue/chat", message, headerAccessorForUser.getMessageHeaders());
            }
        }
    }
}
