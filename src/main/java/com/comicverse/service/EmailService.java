package com.comicverse.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Gửi text email
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, "utf-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);
        helper.setFrom("comicverseservice@gmail.com");

        mailSender.send(mail);
    }

    // Gửi HTML email
    public void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, "utf-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        helper.setFrom("comicverseservice@gmail.com");

        mailSender.send(mail);
    }
}
