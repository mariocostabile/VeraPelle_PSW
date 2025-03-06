package psw.verapelle.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auth/hello")
    public String hello() {
        return "Hello World from Demo ADMIN Controller";
    }

    @GetMapping("/hello2")
    public String hello2() {
        return "Hello World from Demo Controller";
    }
}
