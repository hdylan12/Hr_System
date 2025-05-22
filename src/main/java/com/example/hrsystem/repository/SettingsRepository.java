package com.example.hrsystem.repository;

import com.example.hrsystem.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, String> {
}