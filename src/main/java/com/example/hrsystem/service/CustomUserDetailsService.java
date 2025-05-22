package com.example.hrsystem.service;

import com.example.hrsystem.model.User;
import com.example.hrsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("START - Attempting to load user: {}", username);

        // Fetch user from the database using UserRepository
        User user = userRepository.findById(username)
                .orElseThrow(() -> {
                    logger.warn("User not found in database: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        logger.info("User found - Username: {}, Role: {}, Password: [PROTECTED]",
                user.getUsername(), user.getRole());

        // Convert the User.Role enum to a Spring Security authority (e.g., ROLE_ADMIN)
        String role = "ROLE_" + user.getRole().name();
        logger.info("Constructed authority for user {}: {}", user.getUsername(), role);

        // Return Spring Security UserDetails object
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
        );

        logger.info("Returning UserDetails for: {}", user.getUsername());
        return userDetails;
    }
}