package com.example.hrsystem.service;

import com.example.hrsystem.model.UserSession;
import com.example.hrsystem.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class UserSessionService {
    private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);
    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public void addSession(String username, String sessionId) {
        logger.info("Adding session for user: {}", username);
        try {
            UserSession userSession = new UserSession();
            userSession.setUsername(username);
            userSession.setSessionId(sessionId);
            userSession.setStartTime(LocalDateTime.now());
            userSession.setActive(true);
            userSession.setEndTime(null);
            userSessionRepository.save(userSession);
            logger.info("Session added for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to add session for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to add session", e);
        }
    }

    @Transactional
    public void removeSession(String sessionId) {
        logger.info("Removing session with sessionId: {}", sessionId);
        try {
            userSessionRepository.deleteById(sessionId);
            logger.info("Session removed with sessionId: {}", sessionId);
        } catch (Exception e) {
            logger.error("Failed to remove session with sessionId {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove session", e);
        }
    }

    @Transactional
    public void removeSessionByUsername(String username) {
        logger.info("Removing session for user: {}", username);
        try {
            userSessionRepository.deleteByUsername(username);
            logger.info("Session removed for user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to remove session for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to remove session", e);
        }
    }

    public CompletableFuture<Long> getConnectedUsersCount() {
        logger.info("Fetching connected users count");
        try {
            long count = userSessionRepository.countByActiveTrue();
            logger.info("Connected users count: {}", count);
            return CompletableFuture.completedFuture(count);
        } catch (Exception e) {
            logger.error("Failed to fetch connected users count: {}", e.getMessage(), e);
            CompletableFuture<Long> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}