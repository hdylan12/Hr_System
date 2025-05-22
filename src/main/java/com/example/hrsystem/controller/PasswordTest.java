package com.example.hrsystem.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordTest {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    public void testPassword() {
        String rawPassword = "admin123";
        String storedHash = "$2a$10$qWYJhI/vILgz0aMXXVlcg.QDCzaLG1F5lkVVVtIeXUU0ioOB.Zp.W";
        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        System.out.println("Password matches: " + matches); // Should print "Password matches: true"
    }
}