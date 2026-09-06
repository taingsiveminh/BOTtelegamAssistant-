package com.example.assistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    @GetMapping({"/", "/admin", "/dashboard"})
    public String adminIndex() {
        return "redirect:/admin/index.html";
    }
}
