package com.comicverse.repository;

import com.comicverse.model.AdminOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminOtpRepository extends JpaRepository<AdminOtp, Long> {

    // Lấy TẤT CẢ OTP theo email (không giới hạn 1 OTP)
    List<AdminOtp> findByEmail(String email);
}
