package com.example.hrsystem.repository;

import com.example.hrsystem.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {
}