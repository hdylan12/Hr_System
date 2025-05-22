package com.example.hrsystem.config;

import com.example.hrsystem.service.RequestService;
import com.example.hrsystem.service.UserSessionService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserSessionService userSessionService;
    private final RequestService requestService;

    public GlobalModelAttributes(UserSessionService userSessionService, RequestService requestService) {
        this.userSessionService = userSessionService;
        this.requestService = requestService;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        try {
            model.addAttribute("connectedUsers", userSessionService.getConnectedUsersCount().join());
            model.addAttribute("activeRequests", requestService.getActiveRequestsCount());
        } catch (Exception e) {
            model.addAttribute("connectedUsers", 0);
            model.addAttribute("activeRequests", 0);
        }
    }
}