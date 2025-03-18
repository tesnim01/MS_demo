package com.example.microservice2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/serviceB")
public class Controller {
    @GetMapping("/displayMessage")
    public ResponseEntity<String> showMessage(){
        return ResponseEntity.ok("Microservice 2 controller executed");
    }
}

