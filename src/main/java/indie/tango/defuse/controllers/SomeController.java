package indie.tango.defuse.controllers;

import indie.tango.defuse.models.FriendsModel;
import indie.tango.defuse.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/getInfo")
public class SomeController {
    @Autowired
    private GameService gameService;

    @GetMapping("/getFriends")
    public ResponseEntity<FriendsModel> getFriends(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(gameService.getFriends(token));
    }
    @GetMapping("/addFriends")
    public ResponseEntity<String> addToFriend(@RequestHeader("Authorization") String token, @RequestHeader("friend") String friend) {
        gameService.addFriends(token, friend);
        return ResponseEntity.ok("OK");
    }
}
