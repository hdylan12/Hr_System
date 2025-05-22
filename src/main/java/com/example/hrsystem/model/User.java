package com.example.hrsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "profile_pic")
    private byte[] profilePic;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.EMPLOYEE;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "contact", length = 50)
    private String contact;

    @Column(name = "dob")
    private LocalDate dob;

    public enum Role {
        ADMIN,
        HR_MANAGER,
        EMPLOYEE
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", profilePic=" + (profilePic != null ? "[BINARY DATA]" : "null") +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                ", dob=" + dob +
                '}';
    }
}