package com.example.hrsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ActiveRequestsCounter")
public class ActiveRequestsCounter {
    @Id
    private Integer id;

    @Column(nullable = false)
    private Long counter = 0L;
}