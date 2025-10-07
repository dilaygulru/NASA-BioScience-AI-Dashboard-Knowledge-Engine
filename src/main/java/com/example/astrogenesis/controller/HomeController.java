package com.example.astrogenesis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "nasa"; // templates/nasa.html - Custom landing page
    }

    @GetMapping("/index")
    public String oldIndex() {
        return "index"; // templates/index.html - Old homepage
    }

    @GetMapping("/nasa")
    public String nasaHome() {
        return "nasa"; // templates/nasa.html
    }

    @GetMapping("/about")
    public String about() {
        return "about"; // templates/about.html - About page
    }
}

