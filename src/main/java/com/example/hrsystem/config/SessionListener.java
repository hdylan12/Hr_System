package com.example.hrsystem.config;

import com.example.hrsystem.service.UserSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Component
public class SessionListener implements HttpSessionListener {

    private final UserSessionService userSessionService;

    public SessionListener(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @Override
    @EventListener
    public void sessionCreated(HttpSessionEvent event) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String userId = attr.getRequest().getUserPrincipal() != null ? attr.getRequest().getUserPrincipal().getName() : null;
        if (userId != null) {
            String sessionId = event.getSession().getId();
            userSessionService.addSession(userId, sessionId);
        }
    }

    @Override
    @EventListener
    public void sessionDestroyed(HttpSessionEvent event) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String userId = attr.getRequest().getUserPrincipal() != null ? attr.getRequest().getUserPrincipal().getName() : null;
        if (userId != null) {
            userSessionService.removeSession(userId);
        }
    }

    @EventListener
    public void sessionDestroyed(SessionDestroyedEvent event) {
        for (SecurityContext securityContext : event.getSecurityContexts()) {
            String userId = securityContext.getAuthentication().getName();
            userSessionService.removeSession(userId);
            break; // Typically, only one context exists, so we can break after the first
        }
    }
}