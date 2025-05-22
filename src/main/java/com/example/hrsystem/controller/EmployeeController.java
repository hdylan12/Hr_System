package com.example.hrsystem.controller;

import com.example.hrsystem.model.Employee;
import com.example.hrsystem.model.Position;
import com.example.hrsystem.model.Qualification;
import com.example.hrsystem.model.User;
import com.example.hrsystem.service.EmployeeService;
import com.example.hrsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final UserService userService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, UserService userService) {
        this.employeeService = employeeService;
        this.userService = userService;
    }

    @GetMapping("/employees")
    public String listEmployees(Model model, Authentication authentication) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "admin-employees";
    }

    @GetMapping("/employees/add")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "fragments/add-employee-form";
    }



    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Integer id, Model model, Authentication authentication) {
        try {
            employeeService.deleteEmployee(id);
            logger.info("Employee deleted: {}", id);
            model.addAttribute("message", "Employee deleted successfully!");
        } catch (Exception e) {
            logger.error("Failed to delete employee with ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Failed to delete employee: " + e.getMessage());
        }
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("username", authentication.getName());
        model.addAttribute("adminProfilePic", "/images/default-avatar.png");
        return "admin-employees";
    }
}