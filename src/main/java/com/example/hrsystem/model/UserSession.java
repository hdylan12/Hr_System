package com.example.hrsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_session") // Match the table name in the database
public class UserSession {
    @Id
    @Column(name = "session_id") // session_id is the primary key in the table
    private String sessionId;

    @Column(name = "username", nullable = false) // Match the username column
    private String username;

    @Column(name = "start_time", nullable = false) // Match the start_time column
    private LocalDateTime startTime;

    @Column(name = "end_time") // Match the end_time column
    private LocalDateTime endTime;

    @Column(name = "active", nullable = false) // Match the active column
    private boolean active;
}