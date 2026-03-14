package com.comicverse.config;

import com.comicverse.model.User;
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
                // ======================================================
                // 1. CÁC TRANG CÔNG KHAI (AI CŨNG XEM ĐƯỢC)
                // ======================================================
            	.requestMatchers("/comment/load-more").permitAll()
                // Tài nguyên tĩnh (CSS, JS, Ảnh...)
            		// Trong SecurityConfig.java
           		.requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/avatars/**").permitAll()
              
                // Trang chủ & Auth
                .requestMatchers("/", "/home", "/login", "/register", "/forgot-password", "/reset-password").permitAll()
                
                // Đọc truyện & Chi tiết truyện
                .requestMatchers("/truyen/**").permitAll()
                
                
                // Mở khóa cho Sidebar Thể loại & Tìm kiếm
                .requestMatchers("/category/**").permitAll() 
                .requestMatchers("/search/**").permitAll()
                
                // Mở khóa cho API (để khung tìm kiếm gợi ý hoạt động)
                .requestMatchers("/api/**").permitAll() 

                // ======================================================
                // 2. CÁC TRANG CẦN ĐĂNG NHẬP (INTERACTIVE FEATURES)
                // ======================================================
                
                // Quản lý truyện & Hồ sơ cá nhân
                .requestMatchers("/profile/**", "/change-password").authenticated()
                .requestMatchers("/comic/manage", "/comic/create", "/comic/edit/**", "/comic/delete/**").authenticated()
                
                // Tương tác: Bình luận, Đánh giá, Theo dõi
                .requestMatchers("/comment/**", "/review/**", "/rating/**").authenticated()
                .requestMatchers("/follow/**", "/unfollow/**").authenticated() 

                // Admin Panel
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                
                // Các link còn lại chặn hết cho chắc
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // 1. LẤY THÔNG TIN USER & LƯU SESSION
                    HttpSession session = request.getSession();
                    String email = authentication.getName();
                    var userOpt = userRepository.findByEmail(email);
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        session.setAttribute("user", user);
                        session.setAttribute("avatar", user.getAvatar());
                        session.setAttribute("username", user.getUsername());
                        
                        // 2. XỬ LÝ ADMIN (OTP)
                        if (user.getRole() == User.Role.ADMIN) {
                            otpService.sendOtpToAdmin(email);
                            session.setAttribute("pendingAdminEmail", email);
                            response.sendRedirect("/admin/verify-otp");
                            return; // Dừng tại đây nếu là Admin
                        }
                    }

                    // 3. ✅ QUAN TRỌNG: KIỂM TRA REDIRECT (QUAY VỀ TRANG CŨ)
                    // Lấy giá trị từ ô input hidden name="redirect" trong form login
                    String redirectUrl = request.getParameter("redirect");

                    if (redirectUrl != null && !redirectUrl.isEmpty()) {
                        // Nếu có link cũ -> Quay lại đó
                        response.sendRedirect(redirectUrl);
                    } else {
                        // Nếu không -> Về trang chủ
                        response.sendRedirect("/home");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    // Xử lý khi đăng nhập sai
                    request.getSession().setAttribute("loginError", "Email hoặc mật khẩu không đúng!");
                    response.sendRedirect("/login?error");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home") // Về Home sau khi logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .authenticationProvider(authProvider());

        return http.build();
    }
}