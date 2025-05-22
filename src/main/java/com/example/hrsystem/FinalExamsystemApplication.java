package com.example.hrsystem;

import com.example.hrsystem.service.DataInitializerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FinalExamsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalExamsystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(DataInitializerService dataInitializerService) {
        return (args) -> {
            dataInitializerService.initializeSampleData();
        };
    }
}