package com.example.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class HelloController {
    @GetMapping("/hello")
    ResponseEntity<String> hello(){
        log.info("hello controller");
        return ResponseEntity.ok().body("hello");
    }
}
