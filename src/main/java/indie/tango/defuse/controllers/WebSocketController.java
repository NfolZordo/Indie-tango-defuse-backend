package indie.tango.defuse.controllers;

import indie.tango.defuse.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class WebSocketController {

    @Autowired
    private GameService gameService;

    @MessageMapping("/createGame")
    @SendToUser("/queue/gameCode")
    public String createGame(SimpMessageHeaderAccessor headerAccessor) {
        return gameService.createGame(headerAccessor);
    }

    @MessageMapping("/getTask")
    @SendToUser("/queue/getTask")
    public ResponseEntity<byte[]> getTask(String gameCode) throws IOException {
        byte[] imageBytes = gameService.getTask(gameCode);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }

    @MessageMapping("/joinGame")
    @SendToUser("/queue/joinResult")
    public String joinGame(@Payload String gameCode, SimpMessageHeaderAccessor headerAccessor) {
        return gameService.joinGame(gameCode, headerAccessor);
    }

    @MessageMapping("/startTimer")
    public void startTimer(SimpMessageHeaderAccessor headerAccessor, String gameMode) {
        gameService.startTimer(headerAccessor, gameMode);
    }

    @MessageMapping("/stopTimer")
    public void stopTimer(SimpMessageHeaderAccessor headerAccessor) {
        gameService.stopTimer(headerAccessor);
    }

    @MessageMapping("/doStep")
    @SendToUser("/queue/doStep")
    public ResponseEntity<String> doStep(SimpMessageHeaderAccessor headerAccessor, String button) {
        return ResponseEntity.ok(gameService.doStep(headerAccessor,button));
    }

}
