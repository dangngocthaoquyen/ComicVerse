package com.comicverse.service;

import com.comicverse.model.AdminOtp;
import com.comicverse.repository.AdminOtpRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private AdminOtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    // Gửi OTP admin
    public void sendOtpToAdmin(String email) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        AdminOtp adminOtp = new AdminOtp();
        adminOtp.setEmail(email);
        adminOtp.setOtp(otp);
        adminOtp.setExpiry(LocalDateTime.now().plusMinutes(3));

        otpRepository.save(adminOtp);

        try {
            emailService.sendHtmlEmail(
                    email,
                    "Mã OTP đăng nhập quản trị",
                    "<h2>Mã OTP của bạn: <b>" + otp + "</b></h2>" +
                    "<p>Mã có hiệu lực trong 3 phút.</p>"
            );
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi gửi email OTP!");
        }
    }

    // Kiểm tra OTP
    public boolean verifyOtp(String email, String otp) {

        List<AdminOtp> list = otpRepository.findByEmail(email);
        if (list.isEmpty()) return false;

        AdminOtp stored = list.get(list.size() - 1); // lấy bản ghi mới nhất

        if (!stored.getOtp().equals(otp)) return false;
        if (stored.getExpiry().isBefore(LocalDateTime.now())) return false;

        otpRepository.delete(stored);
        return true;
    }
}
