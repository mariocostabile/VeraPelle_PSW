package psw.verapelle.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping
    public String hello() {
        return "Hello World from Demo Controller";
    }

    @GetMapping("/hello2")
    public String hello2() {
        return "Hello World from Demo Controller - Admin";
    }
}
