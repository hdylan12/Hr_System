package com.example.hrsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "qualification")
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qualification_id")
    private Integer qualificationId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QualificationType type;

    public enum QualificationType {
        Diploma,
        Bachelor,
        Master,
        PhD,
        Professor
    }

    @Override
    public String toString() {
        return "Qualification{" +
                "qualificationId=" + qualificationId +
                ", employee=" + (employee != null ? employee.getEmployeeId() : "null") +
                ", type=" + type +
                '}';
    }
}