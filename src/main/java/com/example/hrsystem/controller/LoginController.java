package com.example.hrsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        // Log the login page access with query parameters
        logger.info("Accessing login page with params: error={}, logout={}, expired={}", error, logout, expired);

        // Handle error scenarios
        if ("unauthorized".equals(error)) {
            logger.warn("Unauthorized access attempt detected");
            model.addAttribute("errorMessage", "Access denied. Only Admin or HR Manager roles are authorized.");
        } else if ("server".equals(error)) {
            logger.error("Server error during authentication redirection");
            model.addAttribute("errorMessage", "A server error occurred. Please try again later.");
        } else if (error != null) {
            logger.warn("Invalid login attempt");
            model.addAttribute("errorMessage", "Invalid username or password.");
        }

        // Handle session expiration
        if (expired != null) {
            logger.info("Session expired for a user");
            model.addAttribute("infoMessage", "Your session has expired. Please log in again.");
        }

        // Handle successful logout
        if (logout != null) {
            logger.info("User logged out successfully");
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        return "login";
    }
}