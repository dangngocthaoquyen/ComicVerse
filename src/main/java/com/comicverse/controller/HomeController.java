package com.comicverse.controller;

import com.comicverse.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @Autowired
    private EmailService emailService;

    // Trang chủ ComicVerse
    @GetMapping("/")
    public String home() {
        return "index";
    }

    
}
