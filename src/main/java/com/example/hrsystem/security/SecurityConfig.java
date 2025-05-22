package com.example.hrsystem.security;

import com.example.hrsystem.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        logger.info("SecurityConfig initialized with CustomUserDetailsService");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring SecurityFilterChain");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/img/**", "/js/**", "/static/**", "/resources/**", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/hr/**").hasAnyRole("HR_MANAGER", "ADMIN")
                        .requestMatchers("/hr/**").hasRole("HR_MANAGER")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler())
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/login?expired")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.info("Unauthenticated access to {}, redirecting to /login", request.getRequestURI());
                            response.sendRedirect("/login");
                        })
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                );

        logger.info("SecurityFilterChain configured successfully");
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        logger.info("Providing CustomUserDetailsService bean");
        return customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() { // Explicitly define as BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            try {
                HttpSession session = request.getSession(true);
                String userId = authentication.getName();
                session.setAttribute("userId", userId);

                if (authentication.getAuthorities().isEmpty()) {
                    logger.error("No authorities found for user: {}", userId);
                    response.sendRedirect("/login?error=unauthorized");
                    return;
                }

                String role = authentication.getAuthorities().iterator().next().getAuthority();
                logger.info("Authentication successful for user: {}, role: {}", userId, role);

                switch (role) {
                    case "ROLE_ADMIN":
                        logger.info("Redirecting user {} with role {} to /admin/dashboard", userId, role);
                        response.sendRedirect("/admin/dashboard");
                        break;
                    case "ROLE_HR_MANAGER":
                        logger.info("Redirecting user {} with role {} to /hr/dashboard", userId, role);
                        response.sendRedirect("/hr/dashboard");
                        break;
                    case "ROLE_EMPLOYEE":
                        logger.info("Redirecting user {} with role {} to /employee/dashboard", userId, role);
                        response.sendRedirect("/employee/dashboard");
                        break;
                    default:
                        logger.warn("Unknown role: {} for user: {}. Redirecting to /login?error=unauthorized", role, userId);
                        response.sendRedirect("/login?error=unauthorized");
                        break;
                }
            } catch (Exception e) {
                logger.error("Error during authentication success handling for user: {}. Exception: {}", authentication.getName(), e.getMessage(), e);
                response.sendRedirect("/login?error=server");
            }
        };
    }
}