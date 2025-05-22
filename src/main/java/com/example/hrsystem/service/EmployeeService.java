package com.example.hrsystem.service;

import com.example.hrsystem.model.Employee;
import com.example.hrsystem.model.Position;
import com.example.hrsystem.model.Qualification;
import com.example.hrsystem.repository.EmployeeRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.micrometer.common.util.StringUtils.truncate;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee addEmployee(Employee employee) {
        logger.info("Attempting to add employee: {}", employee.toString());

        // Set employee reference for nested objects
        if (employee.getPosition() != null) {
            employee.getPosition().setEmployee(employee);
            logger.info("Set position for employee: {}", employee.getEmployeeId());
        }
        if (employee.getQualifications() != null) {
            employee.getQualifications().forEach(qualification -> {
                qualification.setEmployee(employee);
            });
            logger.info("Set employee for {} qualifications", employee.getQualifications().size());
        }

        try {
            Employee savedEmployee = employeeRepository.save(employee);
            logger.info("Employee added successfully with ID: {}", savedEmployee.getEmployeeId());
            return savedEmployee;
        } catch (Exception e) {
            logger.error("Failed to add employee: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add employee: " + e.getMessage(), e);
        }
    }


    @Transactional
    public void updateEmployee(Integer employeeId, Employee updatedEmployee) {
        logger.info("Updating employee with ID: {}", employeeId);

        try {
            Optional<Employee> existingEmployeeOpt = employeeRepository.findById(employeeId);
            if (!existingEmployeeOpt.isPresent()) {
                logger.error("Employee with ID {} not found", employeeId);
                throw new RuntimeException("Employee with ID " + employeeId + " not found");
            }

            Employee existingEmployee = existingEmployeeOpt.get();
            // Update fields
            existingEmployee.setUsername(truncate(updatedEmployee.getUsername(), 50));
            existingEmployee.setEmail(truncate(updatedEmployee.getEmail(), 50));
            existingEmployee.setPassword(truncate(updatedEmployee.getPassword(), 50));
            existingEmployee.setFirstName(truncate(updatedEmployee.getFirstName(), 50));
            existingEmployee.setLastName(truncate(updatedEmployee.getLastName(), 50));
            existingEmployee.setDob(updatedEmployee.getDob());
            existingEmployee.setPhoneNumber(truncate(updatedEmployee.getPhoneNumber(), 50));
            existingEmployee.setProfilePic(updatedEmployee.getProfilePic());
            existingEmployee.setCreatedBy(truncate(updatedEmployee.getCreatedBy(), 50));

            // Update position (1:1 relationship)
            if (updatedEmployee.getPosition() != null) {
                Position position = updatedEmployee.getPosition();
                position.setEmployee(existingEmployee);
                existingEmployee.setPosition(position);
            } else {
                existingEmployee.setPosition(null);
            }

            // Update qualifications (1:many relationship)
            Hibernate.initialize(existingEmployee.getQualifications()); // Initialize the lazy collection
            if (updatedEmployee.getQualifications() != null) {
                existingEmployee.getQualifications().clear();
                updatedEmployee.getQualifications().forEach(qualification -> {
                    qualification.setEmployee(existingEmployee);
                    existingEmployee.getQualifications().add(qualification);
                });
            } else {
                existingEmployee.getQualifications().clear(); // Clear qualifications if none provided
            }

            employeeRepository.save(existingEmployee);
            logger.info("Employee updated successfully with ID: {}", employeeId);
        } catch (Exception e) {
            logger.error("Failed to update employee with ID {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Failed to update employee: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an employee by their employee ID.
     *
     * @param employeeId The ID of the employee to delete.
     */
    public void deleteEmployee(Integer employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException("Employee with ID " + employeeId + " not found");
        }
        employeeRepository.deleteById(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        try {
            logger.info("Fetching all employees");
            List<Employee> employees = employeeRepository.findAllWithAssociations();
            // Eagerly initialize qualifications
            for (Employee employee : employees) {
                Hibernate.initialize(employee.getQualifications());
                logger.debug("Initialized qualifications for employee ID: {}", employee.getEmployeeId());
            }
            return employees;
        } catch (Exception e) {
            logger.error("Failed to fetch employees: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch employees: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Integer employeeId) {
        logger.info("Fetching employee with ID: {}", employeeId);
        try {
            Optional<Employee> employee = employeeRepository.findById(employeeId);
            if (employee.isPresent()) {
                Hibernate.initialize(employee.get().getQualifications()); // Initialize qualifications
                logger.info("Found employee with ID {}: {}", employeeId, employee.get());
                return employee.get();
            }
            logger.warn("Employee with ID {} not found", employeeId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch employee with ID {}: {}", employeeId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch employee with ID: " + employeeId, e);
        }
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByUsername(String username) {
        logger.info("Fetching employee with username: {}", username);
        try {
            Optional<Employee> employee = employeeRepository.findByUsernameWithAssociations(username);
            if (employee.isPresent()) {
                Hibernate.initialize(employee.get().getQualifications()); // Initialize qualifications
                logger.info("Found employee with username {}: {}", username, employee.get());
                return employee.get();
            }
            logger.warn("Employee with username {} not found", username);
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch employee with username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch employee with username: " + username, e);
        }
    }

    public Employee findEmployeeByIdOrUsername(String query) {
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Query is null or empty, cannot find employee");
            return null;
        }

        try {
            // Try parsing the query as an employee ID (Integer)
            Integer employeeId = Integer.parseInt(query);
            logger.info("Searching for employee by ID: {}", employeeId);
            return employeeRepository.findById(employeeId)
                    .orElseGet(() -> {
                        logger.info("Employee not found by ID: {}", employeeId);
                        return null;
                    });
        } catch (NumberFormatException e) {
            // If query is not a valid Integer, treat it as a username
            logger.info("Query {} is not a valid ID, searching by username", query);
            return employeeRepository.findByUsername(query)
                    .orElseGet(() -> {
                        logger.info("Employee not found by username: {}", query);
                        return null;
                    });
        }
    }
}