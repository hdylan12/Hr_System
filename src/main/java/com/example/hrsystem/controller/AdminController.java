package com.example.hrsystem.controller;

import com.example.hrsystem.model.Settings;
import com.example.hrsystem.model.User;
import com.example.hrsystem.repository.SettingsRepository;
import com.example.hrsystem.service.RequestService;
import com.example.hrsystem.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final RequestService requestService;
    private final SessionRegistry sessionRegistry;
    private final SettingsRepository settingsRepository;

    public AdminController(UserService userService, RequestService requestService,
                           SessionRegistry sessionRegistry, SettingsRepository settingsRepository) {
        this.userService = userService;
        this.requestService = requestService;
        this.sessionRegistry = sessionRegistry;
        this.settingsRepository = settingsRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletResponse response, Authentication authentication) throws InterruptedException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        CompletableFuture<Void> activeRequestFuture = requestService.incrementActiveRequest();
        activeRequestFuture.join();
        long activeRequests = requestService.getActiveRequestsCount();
        long connectedUsers = getConnectedUsers();
        List<User> users = userService.getAllUsers();
        logger.info("Dashboard - Active Requests: {}, Connected Users: {}, Users: {}", activeRequests, connectedUsers, users.size());
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        model.addAttribute("connectedUsers", connectedUsers);
        model.addAttribute("activeRequests", activeRequests);
        model.addAttribute("users", users);
        return "admin-dashboard";
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        Map<String, Object> settings = getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        logger.info("Settings page accessed by {}", authentication.getName());
        return "settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> settings, Model model, Authentication authentication) {
        try {
            settings.forEach((key, value) -> {
                Settings setting = new Settings(key, value);
                settingsRepository.save(setting);
            });
            logger.info("System settings updated: {}", settings);
            model.addAttribute("message", "Settings updated successfully!");
        } catch (Exception e) {
            logger.error("Failed to update settings: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update settings: " + e.getMessage());
        }
        model.addAttribute("settings", settings);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "settings";
    }

    @GetMapping("/content/{type}")
    public String getContent(@PathVariable("type") String contentType, Model model, Authentication authentication) throws InterruptedException {
        logger.info("Fetching content for type: {}", contentType);
        switch (contentType) {
            case "dashboard":
                model.addAttribute("connectedUsers", getConnectedUsers());
                model.addAttribute("activeRequests", getActiveRequests());
                model.addAttribute("users", userService.getAllUsers());
                return "fragments/dashboard-content";
            case "employees":
                model.addAttribute("users", userService.getAllUsers());
                return "fragments/employees-content";
            case "reports":
                return "fragments/reports-content";
            case "settings":
                model.addAttribute("settings", getSettings());
                return "fragments/settings-content";
            case "profile":
                model.addAttribute("username", authentication.getName());
                model.addAttribute("email", "admin@example.com"); // Replace with actual user email if available
                return "fragments/profile-content";
            case "add-user":
                logger.info("Loading add-user form");
                model.addAttribute("user", new User());
                return "fragments/add-user";
            default:
                logger.warn("Unknown content type: {}", contentType);
                return "fragments/error-content";
        }
    }

    @PostMapping("/content/add-user")
    public String addUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "dob", required = false) String dob,
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "profilePicUpload", required = false) MultipartFile profilePicUpload,
            @RequestParam(value = "role", required = false) String role,
            Model model) {
        logger.info("Processing add-user request with manual binding");

        // Create a new User object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        if (dob != null && !dob.trim().isEmpty()) {
            user.setDob(LocalDate.parse(dob));
        }
        user.setContact(contact);

        // Handle profile picture upload
        if (profilePicUpload != null && !profilePicUpload.isEmpty()) {
            try {
                user.setProfilePic(profilePicUpload.getBytes());
                logger.info("Profile picture uploaded for user: {}", username);
            } catch (IOException e) {
                logger.error("Failed to upload profile picture for user {}: {}", username, e.getMessage(), e);
                model.addAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                return "fragments/add-user";
            }
        } else {
            user.setProfilePic(null);
            logger.info("No profile picture provided for user: {}", username);
        }

        // Handle role
        if (role != null && !role.trim().isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(role));
                logger.info("Set role: {}", role);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role: {}", role);
                model.addAttribute("error", "Invalid role: " + role);
                return "fragments/add-user";
            }
        } else {
            user.setRole(User.Role.EMPLOYEE); // Default role
            logger.info("No role provided, setting default role to EMPLOYEE");
        }

        // Manual validation
        List<String> errors = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            errors.add("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.add("Email should be valid");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.add("Password is required");
        }

        if (!errors.isEmpty()) {
            logger.error("Manual validation errors: {}", errors);
            model.addAttribute("user", user);
            model.addAttribute("error", "Please correct the errors in the form: " + errors);
            return "fragments/add-user";
        }

        logger.info("Received user data: username={}, email={}", username, email);
        logger.info("Received role: {}", role);

        try {
            logger.info("Calling userService.createUser for user: {}", user.getUsername());
            userService.createUser(user);
            logger.info("User created successfully: {}", user.getUsername());
            model.addAttribute("message", "User added successfully!");
            model.addAttribute("users", userService.getAllUsers());
        } catch (Exception e) {
            logger.error("Failed to create user: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to add user: " + e.getMessage());
            model.addAttribute("user", user);
            return "fragments/add-user";
        }
        return "fragments/employees-content";
    }

    @PostMapping("/content/delete-user/{username}")
    public String deleteUser(@PathVariable("username") String username, Model model) {
        try {
            userService.deleteUser(username);
            model.addAttribute("message", "User deleted successfully");
        } catch (Exception e) {
            logger.error("Failed to delete user with username {}: {}", username, e.getMessage(), e);
            model.addAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        model.addAttribute("users", userService.getAllUsers());
        return "fragments/employees-content";
    }

    private long getConnectedUsers() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> {
                    org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) principal;
                    return userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_HR_MANAGER"));
                })
                .map(principal -> sessionRegistry.getAllSessions(principal, false))
                .flatMap(List::stream)
                .filter(sessionInfo -> !sessionInfo.isExpired())
                .count();
    }

    private long getActiveRequests() throws InterruptedException {
        return requestService.getActiveRequestsCount();
    }

    private Map<String, Object> getSettings() {
        try {
            Map<String, Object> settings = new HashMap<>();
            List<Settings> settingsList = settingsRepository.findAll();
            settingsList.forEach(setting -> settings.put(setting.getKey(), setting.getValue()));
            if (settings.isEmpty()) {
                settings.put("maxSessions", "1");
                settings.put("defaultRole", "EMPLOYEE");
            }
            return settings;
        } catch (Exception e) {
            logger.error("Failed to fetch settings: {}", e.getMessage(), e);
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("maxSessions", "1");
            defaultSettings.put("defaultRole", "EMPLOYEE");
            return defaultSettings;
        }
    }

    @PostMapping("/user/create")
    public String createUser(@ModelAttribute User user, Model model) {
        try {
            userService.createUser(user);
            model.addAttribute("message", "User created successfully!");
            logger.info("User created successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
            model.addAttribute("errorMessage", "Failed to create user: " + e.getMessage());
        }
        return "fragments/user-list";
    }

    @GetMapping("/reset-requests")
    public String resetRequests() {
        requestService.resetActiveRequests();
        return "redirect:/admin/dashboard";
    }
}