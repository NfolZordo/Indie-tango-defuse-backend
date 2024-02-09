package indie.tango.defuse.controllers;

import indie.tango.defuse.models.FriendsModel;
import indie.tango.defuse.services.GameService;
import io.micrometer.common.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private GameService gameService;

    @MessageMapping("/createGame")
    @SendToUser("/queue/gameCode")
    public String createGame(SimpMessageHeaderAccessor headerAccessor, @RequestHeader("Authorization") String token) {
        return gameService.createGame(headerAccessor, token);
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
    public String joinGame(SimpMessageHeaderAccessor headerAccessor, @Payload Map<String, String> payload) {
        String gameCode = payload.get("gameCode");
        String token = payload.get("Authorization");
        return gameService.joinGame(gameCode, headerAccessor, token);
    }

    @MessageMapping("/startTimer")
    public void startTimer(SimpMessageHeaderAccessor headerAccessor, @Payload Map<String, String> payload) {
        String gameMode = payload.get("gameMode");
        String token = payload.get("Authorization");
        gameService.startTimer(headerAccessor, gameMode, token);
    }

    @MessageMapping("/stopTimer")
    public void stopTimer(SimpMessageHeaderAccessor headerAccessor, @RequestHeader("Authorization") String token) {
        gameService.stopTimer(headerAccessor, token);
    }

    @MessageMapping("/doStep")
    @SendToUser("/queue/doStep")
    public ResponseEntity<String> doStep(SimpMessageHeaderAccessor headerAccessor, String button) {
        return ResponseEntity.ok(gameService.doStep(headerAccessor,button));
    }
}
