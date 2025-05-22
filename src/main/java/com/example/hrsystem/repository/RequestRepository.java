package com.example.hrsystem.repository;

import com.example.hrsystem.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, String> {
}