package VitAI.injevital.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class controller {
    @GetMapping("test")
    public String Test() {
            return "success!";
        }

}
