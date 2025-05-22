package com.example.hrsystem.controller;

import com.example.hrsystem.model.Employee;
import com.example.hrsystem.model.Position;
import com.example.hrsystem.model.Qualification;
import com.example.hrsystem.model.User;
import com.example.hrsystem.service.EmployeeService;
import com.example.hrsystem.service.RequestService;
import com.example.hrsystem.service.UserService;
import com.example.hrsystem.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/hr")
@PreAuthorize("hasRole('HR_MANAGER')")
public class HRController {
    private static final Logger logger = LoggerFactory.getLogger(HRController.class);
    private final EmployeeService employeeService;
    private final UserService userService;
    @Autowired
    private RequestService requestService;

    @Autowired
    private UserSessionService userSessionService;

    public HRController(EmployeeService employeeService, UserService userService) {
        this.employeeService = employeeService;
        this.userService = userService;
    }

    @GetMapping("/content/add-employee")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "fragments/add-employee-form";
    }

    @GetMapping("/dashboard")
    public String hrDashboard(Model model) {
        // Fetch employees
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);

        // Calculate new hires (employees hired in the last 30 days)
        long newHires = employees.stream()
                .filter(e -> e.getStartDate() != null && e.getStartDate().isAfter(LocalDate.now().minusDays(30)))
                .count();
        model.addAttribute("newHires", newHires);

        // Fetch connected users and active requests
        CompletableFuture<Long> connectedUsers = userSessionService.getConnectedUsersCount();
        long activeRequests = requestService.getActiveRequestsCount();
        model.addAttribute("connectedUsers", connectedUsers);
        model.addAttribute("activeRequests", activeRequests);

        // Generate a list of the last 7 days and their formatted strings
        List<String> recentDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String formattedDate = date.format(formatter);
            recentDates.add(formattedDate);
        }
        model.addAttribute("recentDates", recentDates);

        return "hr-dashboard";
    }

    @GetMapping("/content/{type}")
    public String getContent(@PathVariable("type") String contentType, Model model,@RequestParam(value = "query", required = false) String query
                             ,Authentication authentication) {
        logger.info("Fetching content for type: {}", contentType);
        try {
            switch (contentType) {
                case "dashboard":
                    model.addAttribute("employees", employeeService.getAllEmployees());
                    return "fragments/hr-dashboard-content";
                case "employees":
                    model.addAttribute("employees", employeeService.getAllEmployees());
                    return "fragments/employees-content";
                case "profile":
                    model.addAttribute("username", authentication.getName());
                    model.addAttribute("email", "hr@example.com"); // Replace with actual user email if available
                    return "fragments/profile-content";
                case "add-employee":
                    logger.info("Loading add-employee form");
                    model.addAttribute("employee", new Employee());
                    return "fragments/add-employee-form";
                case "search-employee":
                    logger.info("Loading find-employee form");
                    if (query != null && !query.isEmpty()) {
                        logger.info("Accessing find-employee with query: {}", query);
                        try {
                            Employee employee = employeeService.findEmployeeByIdOrUsername(query);
                            if (employee != null) {
                                model.addAttribute("employee", employee);
                            } else {
                                model.addAttribute("error", "Employee not found");
                            }
                        } catch (Exception e) {
                            logger.error("Error finding employee with query {}: {}", query, e.getMessage());
                            model.addAttribute("error", "Error searching for employee: " + e.getMessage());
                        }
                    } else {
                        logger.info("Loading search-employee form without search");
                    }
                    return "fragments/search-employee :: content";
                case "delete-employee":
                    logger.info("Loading delete-employee form");
                    if (query != null && !query.isEmpty()) {
                        logger.info("Accessing delete-employee with query: {}", query);
                        try {
                            Employee employee = employeeService.findEmployeeByIdOrUsername(query);
                            if (employee != null) {
                                model.addAttribute("employee", employee);
                            } else {
                                model.addAttribute("error", "Employee not found");
                            }
                        } catch (Exception e) {
                            logger.error("Error finding employee with query {}: {}", query, e.getMessage());
                            model.addAttribute("error", "Error searching for employee: " + e.getMessage());
                        }
                    } else {
                        logger.info("Loading delete-employee form without search");
                    }
                    return "fragments/delete-employee-form :: content";
                case "recruitment":
                    return "fragments/recruitment-content"; // Placeholder fragment
                case "attendance":
                    return "fragments/attendance-content"; // Placeholder fragment
                case "employee-actions":
                    return "fragments/employee-actions-content"; // Placeholder fragment
                case "departments":
                    return "fragments/departments-content"; // Placeholder fragment
                case "reports":
                    return "fragments/reports-content"; // Placeholder fragment
                case "company-policy":
                    return "fragments/company-policy-content"; // Placeholder fragment
                case "payroll-calculator":
                    return "fragments/payroll-calculator-content";
                case "employee-stats": // New case
                    logger.info("Loading employee-stats");
                    return "fragments/employee-stats :: content";
                default:
                    logger.warn("Unknown content type: {}", contentType);
                    return "fragments/error-content";
            }
        } catch (Exception e) {
            logger.error("Failed to load content for type {}: {}", contentType, e.getMessage(), e);
            model.addAttribute("error", "Failed to load content: " + e.getMessage());
            return "fragments/error-content";
        }
    }

    @GetMapping("/content/update-employee/{id}")
    public String getUpdateEmployeeForm(@PathVariable("id") Integer id, Model model) {
        logger.info("Fetching employee with ID: {}", id);
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                model.addAttribute("error", "Employee not found with ID: " + id);
                return "fragments/error-content";
            }
            model.addAttribute("employee", employee);
            return "fragments/update-employee-form";
        } catch (Exception e) {
            logger.error("Failed to fetch employee with ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Failed to load employee: " + e.getMessage());
            return "fragments/error-content";
        }
    }

    @PostMapping("/content/add-employee")
    public String addEmployee(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "dob", required = false) String dob,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "profilePicUpload", required = false) MultipartFile profilePicUpload,
            @RequestParam(value = "positionName", required = false) String positionName,
            @RequestParam(value = "positionCode", required = false) Integer positionCode,
            @RequestParam(value = "qualifications", required = false) List<String> qualifications,
            Model model, Authentication authentication) {
        logger.info("Processing add-employee request");

        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword(password);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        if (dob != null && !dob.trim().isEmpty()) {
            employee.setDob(LocalDate.parse(dob));
        }
        employee.setPhoneNumber(phoneNumber);

        // Handle profile picture upload
        if (profilePicUpload != null && !profilePicUpload.isEmpty()) {
            try {
                employee.setProfilePic(profilePicUpload.getBytes());
                logger.info("Profile picture uploaded for employee: {}", username);
            } catch (IOException e) {
                logger.error("Failed to upload profile picture for employee {}: {}", username, e.getMessage(), e);
                model.addAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                model.addAttribute("employee", employee);
                return "fragments/add-employee-form";
            }
        } else {
            employee.setProfilePic(null);
            logger.info("No profile picture provided for employee: {}", username);
        }

        List<String> errors = new ArrayList<>();
        // Required field validation
        if (username == null || username.trim().isEmpty()) errors.add("Username is required");
        if (email == null || email.trim().isEmpty()) errors.add("Email is required");
        else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) errors.add("Email should be valid");
        if (password == null || password.trim().isEmpty()) errors.add("Password is required");
        if (firstName == null || firstName.trim().isEmpty()) errors.add("First name is required");
        if (lastName == null || lastName.trim().isEmpty()) errors.add("Last name is required");

        // Length validation
        if (username != null && username.length() > 50) errors.add("Username must not exceed 50 characters");
        if (email != null && email.length() > 50) errors.add("Email must not exceed 50 characters");
        if (password != null && password.length() > 50) errors.add("Password must not exceed 50 characters");
        if (firstName != null && firstName.length() > 50) errors.add("First name must not exceed 50 characters");
        if (lastName != null && lastName.length() > 50) errors.add("Last name must not exceed 50 characters");
        if (phoneNumber != null && phoneNumber.length() > 50) errors.add("Phone number must not exceed 50 characters");

        if (!errors.isEmpty()) {
            logger.error("Validation errors: {}", errors);
            model.addAttribute("employee", employee);
            model.addAttribute("error", "Please correct the errors in the form: " + errors);
            return "fragments/add-employee-form";
        }

        if (positionName != null && !positionName.trim().isEmpty() && positionCode != null) {
            Position position = new Position();
            position.setName(positionName);
            position.setCode(positionCode);
            employee.setPosition(position);
            logger.info("Set position: {}", position.getName());
        } else {
            employee.setPosition(null);
            logger.info("No position provided, setting position to null");
        }

        if (qualifications != null && !qualifications.isEmpty()) {
            List<Qualification> qualificationList = new ArrayList<>();
            for (String qualStr : qualifications) {
                try {
                    Qualification qual = new Qualification();
                    qual.setType(Qualification.QualificationType.valueOf(qualStr.trim()));
                    qual.setEmployee(employee);
                    qualificationList.add(qual);
                    logger.info("Added qualification: {}", qual.getType());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid qualification type: {}", qualStr);
                    errors.add("Invalid qualification type: " + qualStr);
                }
            }
            if (!errors.isEmpty()) {
                model.addAttribute("employee", employee);
                model.addAttribute("error", "Please correct the errors in the form: " + errors);
                return "fragments/add-employee-form";
            }
            employee.setQualifications(qualificationList);
            logger.info("Set qualifications: {}", qualificationList);
        } else {
            employee.setQualifications(new ArrayList<>());
            logger.info("No qualifications provided, setting qualifications to empty list");
        }

        // Set createdBy using the username
        String createdByUsername = authentication.getName();
        User createdBy = userService.getUserByUsername(createdByUsername);
        if (createdBy == null) {
            logger.error("User with username {} not found", createdByUsername);
            model.addAttribute("error", "Failed to add employee: Creator user not found");
            return "fragments/add-employee-form";
        }
        if (createdByUsername != null && createdByUsername.length() > 50) {
            errors.add("Created by username must not exceed 50 characters");
            model.addAttribute("employee", employee);
            model.addAttribute("error", "Please correct the errors in the form: " + errors);
            return "fragments/add-employee-form";
        }
        employee.setCreatedBy(createdByUsername);

        try {
            Employee savedEmployee = employeeService.addEmployee(employee);
            model.addAttribute("message", "Employee added successfully with ID: " + savedEmployee.getEmployeeId());
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "redirect:/hr/dashboard";
        } catch (Exception e) {
            logger.error("Failed to save employee: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to add employee: " + e.getMessage());
            model.addAttribute("employee", employee);
            return "fragments/add-employee-form";
        }
    }

    @GetMapping("/content/employees")
    public String showEmployees(Model model) {
        logger.info("Fetching employees list for Employee Management");
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
            return "employees-management"; // Return the full page, not just the fragment
        } catch (Exception e) {
            logger.error("Failed to fetch employees: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load employees: " + e.getMessage());
            return "fragments/error-content";
        }
    }

    @GetMapping("/content/update-employee-form")
    public String showUpdateEmployeeForm(@RequestParam("employeeId") Integer employeeId, Model model) {
        logger.info("Fetching employee with ID {} for update form", employeeId);
        try {
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee == null) {
                logger.error("Employee with ID {} not found", employeeId);
                model.addAttribute("error", "Employee not found with ID: " + employeeId);
                return "fragments/error-content";
            }
            model.addAttribute("employee", employee);
            return "fragments/update-employee-form";
        } catch (Exception e) {
            logger.error("Failed to fetch employee for update: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load update form: " + e.getMessage());
            return "fragments/error-content";
        }
    }

    @PostMapping("/content/update-employee")
    public String updateEmployee(
            @RequestParam("employeeId") Integer employeeId,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "dob", required = false) String dob,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "profilePicUpload", required = false) MultipartFile profilePicUpload,
            @RequestParam(value = "positionName", required = false) String positionName,
            @RequestParam(value = "positionCode", required = false) Integer positionCode,
            @RequestParam(value = "qualifications", required = false) List<String> qualifications,
            Model model, Authentication authentication) {
        logger.info("Processing update-employee request for ID: {}", employeeId);
        logger.info("Update endpoint hit for employee ID: {}", employeeId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            model.addAttribute("error", "Employee not found with ID: " + employeeId);
            return "fragments/error-content";
        }

        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword(password);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        if (dob != null && !dob.trim().isEmpty()) {
            employee.setDob(LocalDate.parse(dob));
        } else {
            employee.setDob(null);
        }
        employee.setPhoneNumber(phoneNumber);

        // Handle profile picture upload
        if (profilePicUpload != null && !profilePicUpload.isEmpty()) {
            try {
                employee.setProfilePic(profilePicUpload.getBytes());
                logger.info("Profile picture updated for employee: {}", username);
            } catch (IOException e) {
                logger.error("Failed to upload profile picture for employee {}: {}", username, e.getMessage(), e);
                model.addAttribute("error", "Failed to upload profile picture: " + e.getMessage());
                model.addAttribute("employee", employee);
                return "fragments/update-employee-form";
            }
        } else {
            logger.info("No new profile picture provided for employee: {}", username);
        }

        List<String> errors = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) errors.add("Username is required");
        if (email == null || email.trim().isEmpty()) errors.add("Email is required");
        else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) errors.add("Email should be valid");
        if (password == null || password.trim().isEmpty()) errors.add("Password is required");
        if (firstName == null || firstName.trim().isEmpty()) errors.add("First name is required");
        if (lastName == null || lastName.trim().isEmpty()) errors.add("Last name is required");

        if (!errors.isEmpty()) {
            logger.error("Validation errors: {}", errors);
            model.addAttribute("employee", employee);
            model.addAttribute("error", "Please correct the errors in the form: " + errors);
            return "fragments/update-employee-form";
        }

        if (positionName != null && !positionName.trim().isEmpty() && positionCode != null) {
            Position position = new Position();
            position.setName(positionName);
            position.setCode(positionCode);
            employee.setPosition(position);
            logger.info("Set position: {}", position.getName());
        } else {
            employee.setPosition(null);
            logger.info("No position provided, setting position to null");
        }

        if (qualifications != null && !qualifications.isEmpty()) {
            List<Qualification> qualificationList = new ArrayList<>();
            for (String qualStr : qualifications) {
                try {
                    Qualification qual = new Qualification();
                    qual.setType(Qualification.QualificationType.valueOf(qualStr.trim()));
                    qual.setEmployee(employee);
                    qualificationList.add(qual);
                    logger.info("Added qualification: {}", qual.getType());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid qualification type: {}", qualStr);
                    errors.add("Invalid qualification type: " + qualStr);
                }
            }
            if (!errors.isEmpty()) {
                model.addAttribute("employee", employee);
                model.addAttribute("error", "Please correct the errors in the form: " + errors);
                return "fragments/update-employee-form";
            }
            employee.setQualifications(qualificationList);
            logger.info("Set qualifications: {}", qualificationList);
        } else {
            employee.setQualifications(new ArrayList<>());
            logger.info("No qualifications provided, setting qualifications to empty list");
        }

        // Set createdBy using the username instead of the User object's toString()
        User createdBy = userService.getUserByUsername(authentication.getName());
        if (createdBy == null) {
            logger.error("User with username {} not found", authentication.getName());
            model.addAttribute("error", "Failed to update employee: Creator user not found");
            return "fragments/update-employee-form";
        }
        employee.setCreatedBy(createdBy.getUsername()); // Use username instead of String.valueOf(createdBy)

        try {
            employeeService.updateEmployee(employeeId, employee);
            model.addAttribute("message", "Employee updated successfully with ID: " + employeeId);
            model.addAttribute("employees", employeeService.getAllEmployees());
            // Redirect to the employee management page
            return "redirect:/hr/content/employees";
        } catch (Exception e) {
            logger.error("Failed to update employee: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update employee: " + e.getMessage());
            model.addAttribute("employee", employee);
            return "fragments/update-employee-form";
        }
    }

    @PostMapping("/employee/delete")
    @PreAuthorize("hasRole('HR_MANAGER')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteEmployee(@RequestParam("employeeId") Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            employeeService.deleteEmployee(Math.toIntExact(employeeId));
            response.put("success", true);
            response.put("message", "Employee deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete employee: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/content/find-employee")
    public String findEmployeeForView(@RequestParam(value = "searchQuery", required = false) String searchQuery,
                                      @RequestParam(value = "formType", defaultValue = "find") String formType,
                                      Model model) {
        logger.info("Accessing find-employee with query: {}, formType: {}", searchQuery, formType);
        try {
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                // Handle search logic when searchQuery is provided
                Employee employee = null;
                try {
                    Integer id = Integer.parseInt(searchQuery);
                    employee = employeeService.getEmployeeById(id);
                } catch (NumberFormatException e) {
                    logger.info("Search query is not an ID, trying username: {}", searchQuery);
                    employee = employeeService.getEmployeeByUsername(searchQuery);
                }
                if (employee == null) {
                    logger.warn("Employee not found with ID or username: {}", searchQuery);
                    model.addAttribute("error", "Employee not found with ID or username: " + searchQuery);
                } else {
                    logger.info("Found employee: {}", employee.getUsername());
                    model.addAttribute("employee", employee);
                }
            } else {
                // No searchQuery provided; just load the form
                logger.info("Loading find-employee form without search");
            }
            return "fragments/find-employee";
        } catch (Exception e) {
            logger.error("Failed to process find-employee request: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to process request: " + e.getMessage());
            return "fragments/find-employee";
        }
    }


}