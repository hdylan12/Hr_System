package com.example.hrsystem.repository;

import com.example.hrsystem.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    // Delete all sessions for a given username
    void deleteByUsername(String username);

    // Count all active sessions
    long countByActiveTrue();
}