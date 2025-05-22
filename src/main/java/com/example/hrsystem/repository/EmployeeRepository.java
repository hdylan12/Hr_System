package com.example.hrsystem.repository;

import com.example.hrsystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.position LEFT JOIN FETCH e.qualifications WHERE e.username = :username")
    Optional<Employee> findByUsernameWithAssociations(@Param("username") String username);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.position")
    List<Employee> findAllWithAssociations();

    Optional<Employee> findByUsername(String username);
}