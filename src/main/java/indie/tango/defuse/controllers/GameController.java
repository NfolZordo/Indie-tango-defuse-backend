package indie.tango.defuse.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/game")
public class GameController {
    @GetMapping("get")
    public String getTest(){
        return "mmmmmmmmmmmmmmmmmmm";
    }

}
