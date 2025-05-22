package com.example.hrsystem.service;

import com.example.hrsystem.model.ActiveRequestsCounter;
import com.example.hrsystem.model.Request;
import com.example.hrsystem.repository.ActiveRequestsCounterRepository;
import com.example.hrsystem.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class RequestService {
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    private final ActiveRequestsCounterRepository counterRepository;
    private final RequestRepository requestRepository;

    public RequestService(ActiveRequestsCounterRepository counterRepository, RequestRepository requestRepository) {
        this.counterRepository = counterRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public CompletableFuture<Void> incrementActiveRequest() {
        logger.info("Incrementing active requests counter");
        try {
            counterRepository.incrementCounter();
            logger.info("Active requests incremented successfully");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to increment active requests: {}", e.getMessage(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public long getActiveRequestsCount() {
        logger.info("Fetching active requests count from database");
        try {
            ActiveRequestsCounter counter = counterRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Active requests counter not found"));
            long count = counter.getCounter();
            logger.info("Active requests count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error fetching active requests count: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Transactional
    public void resetActiveRequests() {
        logger.info("Resetting active requests counter");
        try {
            counterRepository.resetCounter();
            logger.info("Active requests counter reset to 0");
        } catch (Exception e) {
            logger.error("Failed to reset active requests: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reset active requests", e);
        }
    }

    @Transactional
    public void createRequest(Request request) {
        logger.info("Creating new request (not affecting counters)");
        try {
            String id = UUID.randomUUID().toString();
            request.setId(id);
            requestRepository.save(request);
            logger.info("Request created with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to create request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create request", e);
        }
    }
}