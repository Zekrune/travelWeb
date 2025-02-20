package com.example.travel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        return "index";
    }
}
