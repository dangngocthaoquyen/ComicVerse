package com.comicverse.config;

import com.comicverse.repository.UserRepository;
import com.comicverse.security.CustomUserDetailsService;
import com.comicverse.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/login", "/register",
                        "/forgot-password", "/reset-password",
                        "/css/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {

                    HttpSession session = request.getSession();
                    String email = authentication.getName();

                    var userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        var user = userOpt.get();

                        session.setAttribute("user", user);
                        session.setAttribute("avatar", user.getAvatar());

                        // ⭐⭐ THÊM DÒNG NÀY — QUAN TRỌNG
                        session.setAttribute("username", user.getUsername());
                        // ---------------------------------

                        // ADMIN → gửi OTP
                        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

                            otpService.sendOtpToAdmin(email);
                            session.setAttribute("pendingAdminEmail", email);

                            response.sendRedirect("/admin/verify-otp");
                            return;
                        }
                    }

                    // USER → home
                    response.sendRedirect("/home");
                })
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            )
            .authenticationProvider(authProvider());

        return http.build();
    }
}
