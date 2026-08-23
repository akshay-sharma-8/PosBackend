package com.example.PosSystem.controller;

import com.example.PosSystem.Model.SupportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;
import jakarta.mail.internet.MimeMessage;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private JavaMailSender mailSender;

    // Pull the email address from your application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostMapping("/ticket")
    public ResponseEntity<?> receiveSupportTicket(@RequestBody SupportRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // FIX 1: You MUST set the From address for Google SMTP to accept it
            helper.setFrom(fromEmail);
            helper.setTo(fromEmail); // Sends the email to yourself
            helper.setReplyTo(request.getEmail()); // Allows you to hit "Reply" to answer the customer
            helper.setSubject("App Support Request from " + request.getName());
            helper.setText("User: " + request.getName() + "\nEmail: " + request.getEmail() + "\n\nIssue Details:\n" + request.getQuery());

            // FIX 2: Clean the Base64 string of any invisible newline characters from Android
            if (request.getScreenshotBase64() != null && !request.getScreenshotBase64().isEmpty()) {
                String cleanBase64 = request.getScreenshotBase64().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                ByteArrayResource resource = new ByteArrayResource(imageBytes);
                helper.addAttachment("screenshot.png", resource);
            }

            mailSender.send(message);
            return ResponseEntity.ok().body(Map.of("message", "Sent successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send email: " + e.getMessage()));
        }
    }
}