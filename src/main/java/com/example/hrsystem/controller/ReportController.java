package com.example.hrsystem.controller;

import com.example.hrsystem.model.Report;
import com.example.hrsystem.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String listReports(Model model, Authentication authentication) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "admin-reports";
    }

    @PostMapping("/reports/generate")
    public String generateReport(Model model, Authentication authentication) {
        Report report = reportService.generateReport();
        logger.info("Report generated: {}", report.getTitle());
        model.addAttribute("message", "Report generated successfully!");
        model.addAttribute("reports", reportService.getAllReports());
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "admin-reports";
    }
}