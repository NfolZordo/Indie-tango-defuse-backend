package indie.tango.defuse.controllers;
import indie.tango.defuse.models.GameSession;
import indie.tango.defuse.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private GameSession gameSession;

    @MessageMapping("/createGame")
    @SendToUser("/topic/gameCode")
    public String createGame(SimpMessageHeaderAccessor headerAccessor) {
        String gameCode = gameSession.generateGameCode();
        String playerSessionId = headerAccessor.getSessionId();
        gameSession.addPlayerSession(gameCode, playerSessionId);
        return gameCode;
    }

    @MessageMapping("/joinGame")
    @SendToUser("/topic/joinResult")
    public String joinGame(String gameCode, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String existingGameCode = gameSession.getGameCodeForPlayer(playerSessionId);
        if (existingGameCode != null) {
            // Користувач вже підключений до гри
            return "Already joined a game";
        }

        // Перевірте, чи існує гра з вказаним кодом, і якщо так, додайте гравця
        // Додайте інші перевірки та логіку за потреби
        if (gameSession.isGameExists(gameCode)) {
            gameSession.addPlayerSession(gameCode, playerSessionId);
            return "Successfully joined the game";
        } else {
            return "Game not found";
        }
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/chat")
    public Message sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        String playerSessionId = headerAccessor.getSessionId();
        String gameCode = gameSession.getGameCodeForPlayer(playerSessionId);

        // Перевірте, чи гравець насправді належить до якої-небудь гри
        if (gameCode != null) {
            // Додайте інші перевірки та логіку за потреби

            // Включіть інформацію про відправника в повідомлення
            message.setSender(playerSessionId);

            return message;
        } else {
            // Гравець не знаходиться в жодній грі
            return null;
        }
    }
}
