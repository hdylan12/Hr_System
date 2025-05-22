package com.example.hrsystem.controller;


import com.example.hrsystem.model.User;
import com.example.hrsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("username", username);
        model.addAttribute("adminProfilePic", "/images/default-avatar.png"); // Match header default
        return "admin-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user, Model model, Authentication authentication) {
        String username = authentication.getName();
        user.setUsername(username); // Prevent username change
        try {
            userService.updateUser(username, user);
            logger.info("Profile updated for user: {}", username);
            model.addAttribute("message", "Profile updated successfully!");
        } catch (Exception e) {
            logger.error("Failed to update profile for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        model.addAttribute("user", user);
        model.addAttribute("username", username);
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "admin-profile";
    }
}