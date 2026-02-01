package com.comicverse.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /* ===== GỬI TEXT EMAIL (dùng cho thông báo đơn giản) ===== */
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.setFrom("comicverseservice@gmail.com");

            mailSender.send(mail);
            System.out.println("✅ Sent TEXT email to: " + to);

        } catch (MessagingException | MailException e) {
            System.out.println("❌ Lỗi gửi TEXT email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /* ===== GỬI HTML EMAIL (OTP, reset password, template đẹp) ===== */
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();

            // true = multipart (an toàn cho HTML, attachment, inline image)
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("comicverseservice@gmail.com");

            mailSender.send(mail);
            System.out.println("✅ Sent HTML email to: " + to);

        } catch (MessagingException | MailException e) {
            System.out.println("❌ Lỗi gửi HTML email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
