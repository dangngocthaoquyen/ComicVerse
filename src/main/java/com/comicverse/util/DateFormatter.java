package com.comicverse.util;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("dateFormatter") // Đặt tên bean là 'dateFormatter' để gọi trong HTML
public class DateFormatter {

    public String format(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        long seconds = duration.getSeconds();

        // 1. Dưới 1 phút -> Vừa xong
        if (seconds < 60) {
            return "Vừa xong";
        }

        // 2. Dưới 1 giờ -> X phút trước
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " phút trước";
        }

        // 3. Dưới 1 ngày -> X giờ trước
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " giờ trước";
        }

        // 4. Dưới 7 ngày -> X ngày trước
        long days = hours / 24;
        if (days < 7) {
            return days + " ngày trước";
        }

        // 5. Cũ hơn 7 ngày -> Hiển thị ngày tháng (01/02/2026)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }
}