package indie.tango.defuse.controllers;

import indie.tango.defuse.models.Message;
import indie.tango.defuse.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private GameService gameService;

    @MessageMapping("/createGame")
    @SendToUser("/queue/gameCode")
    public String createGame(SimpMessageHeaderAccessor headerAccessor) {
        return gameService.createGame(headerAccessor);
    }

    @MessageMapping("/joinGame")
    @SendToUser("/queue/joinResult")
    public String joinGame(String gameCode, SimpMessageHeaderAccessor headerAccessor) {
        return gameService.joinGame(gameCode, headerAccessor);
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        gameService.sendMessage(message, headerAccessor);
    }

    @MessageMapping("/startTimer")
    @SendToUser("/queue/startTimer")
    public void startTimer(Message message, SimpMessageHeaderAccessor headerAccessor) {
        gameService.startTimer(message, headerAccessor);
    }

    @MessageMapping("/stopTimer")
    public void stopTimer(SimpMessageHeaderAccessor headerAccessor) {
        gameService.stopTimer(headerAccessor);
    }
}
