package com.harness.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HarnessTestController {

    @GetMapping("/heartbeat")
    public String heartbeat() {
        System.out.println("Testing testing");
        return "Application is up and running!";
    }
}
