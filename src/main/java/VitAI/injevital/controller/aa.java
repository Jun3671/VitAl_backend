package VitAI.injevital.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class aa {
    @GetMapping("test")
    public String Test() {
        return "success!";
    }
}
