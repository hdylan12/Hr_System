package com.example.hrsystem.service;

import com.example.hrsystem.model.*;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataInitializerService {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializerService.class);

    private final UserService userService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializerService(UserService userService, EmployeeService employeeService,
                                  UserRepository userRepository, EmployeeRepository employeeRepository,
                                  PositionRepository positionRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initializeSampleData() {
        // Create default Admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Admin user not found in app_user table, creating default Admin user");
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            logger.info("Creating Admin user...");
            userService.createUser(admin);
            logger.info("Default Admin created: username={}, password=[PROTECTED]", admin.getUsername());
            if (userRepository.findByUsername("admin").isPresent()) {
                logger.info("Verified: Default Admin user created successfully in app_user table");
            }
        }

        // Create default HR Manager user
        if (userRepository.findByUsername("hr_manager").isEmpty()) {
            logger.info("HR Manager user not found in app_user table, creating default HR Manager user");
            User hrManager = new User();
            hrManager.setUsername("hr_manager");
            hrManager.setEmail("hr.manager@example.com");
            hrManager.setPassword(passwordEncoder.encode("hr123"));
            hrManager.setRole(User.Role.HR_MANAGER);
            logger.info("Creating HR Manager user...");
            userService.createUser(hrManager);
            logger.info("Default HR Manager created: username={}, password=[PROTECTED]", hrManager.getUsername());
            if (userRepository.findByUsername("hr_manager").isPresent()) {
                logger.info("Verified: Default HR Manager user created successfully in app_user table");
            }
        }

        // Check if employees exist, if not create sample employees
        if (employeeRepository.count() == 0) {
            logger.info("No employees found in the employee table, creating sample employees");

            // Create and persist position1
            Position position1 = new Position();
            position1.setCode(1001);
            position1.setName("Software Engineer");
            positionRepository.save(position1);

            // Create sample employee 1
            Employee employee1 = new Employee();
            employee1.setUsername("john.doe");
            employee1.setEmail("john.doe@example.com");
            employee1.setPassword(passwordEncoder.encode("password123"));
            employee1.setFirstName("John");
            employee1.setLastName("Doe");
            employee1.setDob(LocalDate.parse("1990-05-15"));
            employee1.setPhoneNumber("123-456-7890");
            employee1.setProfilePic(new byte[]{});
            employee1.setCreatedBy("admin");
            employee1.setPosition(position1);

            List<Qualification> qualifications1 = new ArrayList<>();
            Qualification qual1 = new Qualification();
            qual1.setType(Qualification.QualificationType.Bachelor);
            qualifications1.add(qual1);
            Qualification qual2 = new Qualification();
            qual2.setType(Qualification.QualificationType.Master);
            qualifications1.add(qual2);
            employee1.setQualifications(qualifications1);

            employeeService.addEmployee(employee1);

            // Create and persist position2
            Position position2 = new Position();
            position2.setCode(1002);
            position2.setName("HR Manager");
            positionRepository.save(position2);

            // Create sample employee 2
            Employee employee2 = new Employee();
            employee2.setUsername("jane.smith");
            employee2.setEmail("jane.smith@example.com");
            employee2.setPassword(passwordEncoder.encode("password123"));
            employee2.setFirstName("Jane");
            employee2.setLastName("Smith");
            employee2.setDob(LocalDate.parse("1985-08-22"));
            employee2.setPhoneNumber("987-654-3210");
            employee2.setProfilePic(new byte[]{});
            employee2.setCreatedBy("admin");
            employee2.setPosition(position2);

            List<Qualification> qualifications2 = new ArrayList<>();
            Qualification qual3 = new Qualification();
            qual3.setType(Qualification.QualificationType.PhD);
            qualifications2.add(qual3);
            employee2.setQualifications(qualifications2);

            employeeService.addEmployee(employee2);
        }
    }
}