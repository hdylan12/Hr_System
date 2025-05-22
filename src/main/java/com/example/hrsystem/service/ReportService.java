package com.example.hrsystem.service;

import com.example.hrsystem.model.Report;
import com.example.hrsystem.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final ReportRepository reportRepository;
    private final RequestService requestService;

    public ReportService(ReportRepository reportRepository, RequestService requestService) {
        this.reportRepository = reportRepository;
        this.requestService = requestService;
    }

    public List<Report> getAllReports() {
        logger.info("Fetching all reports from database");
        try {
            List<Report> reports = reportRepository.findAll();
            logger.info("Fetched {} reports", reports.size());
            return reports;
        } catch (Exception e) {
            logger.error("Error fetching reports: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public Report generateReport() {
        logger.info("Generating new report");
        try {
            long activeRequests = requestService.getActiveRequestsCount();
            Report report = new Report();
            report.setId(UUID.randomUUID().toString());
            report.setTitle("Active Requests Report - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            report.setContent("Total Active Requests: " + activeRequests);
            reportRepository.save(report);
            logger.info("Report generated with ID: {}", report.getId());
            return report;
        } catch (Exception e) {
            logger.error("Error generating report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }
}