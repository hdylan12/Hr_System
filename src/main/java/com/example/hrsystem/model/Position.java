package com.example.hrsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "position")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id") // Use position_id as the primary key
    private Integer positionId; // Rename the field to positionId for clarity

    @Column(name = "code") // Map the code column as a regular field
    private Integer code;

    private String name;

    @OneToOne(mappedBy = "position")
    private Employee employee;

    // Default constructor
    public Position() {
    }

    @Override
    public String toString() {
        return "Position{" +
                "positionId=" + positionId +
                ", code=" + code +
                ", name='" + name + '\'' +
                ", employeeId=" + (employee != null ? employee.getEmployeeId() : null) +
                '}';
    }
}