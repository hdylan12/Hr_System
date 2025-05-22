package com.example.hrsystem.service;

import com.example.hrsystem.model.User;
import com.example.hrsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(User user) {
        logger.info("Creating user with username: {}", user.getUsername());
        try {
            userRepository.save(user);
            logger.info("User created successfully with username: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create user with username {}: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public void updateUser(String username, User updatedUser) {
        logger.info("Updating user with username: {}", username);
        try {
            Optional<User> existingUserOpt = userRepository.findById(username);
            if (!existingUserOpt.isPresent()) {
                logger.error("User with username {} not found", username);
                throw new RuntimeException("User with username " + username + " not found");
            }

            User existingUser = existingUserOpt.get();
            // Update fields
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setProfilePic(updatedUser.getProfilePic());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setContact(updatedUser.getContact());
            existingUser.setDob(updatedUser.getDob());

            userRepository.save(existingUser);
            logger.info("User updated successfully with username: {}", username);
        } catch (Exception e) {
            logger.error("Failed to update user with username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public User getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                logger.info("User found with username: {}", username);
                return userOpt.get();
            } else {
                logger.warn("User with username {} not found", username);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to fetch user with username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        try {
            List<User> users = userRepository.findAll();
            logger.info("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to fetch all users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String username) {
        logger.info("Deleting user with username: {}", username);
        try {
            Optional<User> userOpt = userRepository.findById(username);
            if (!userOpt.isPresent()) {
                logger.error("User with username {} not found", username);
                throw new RuntimeException("User with username " + username + " not found");
            }
            userRepository.deleteById(username);
            logger.info("User deleted successfully with username: {}", username);
        } catch (Exception e) {
            logger.error("Failed to delete user with username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
}