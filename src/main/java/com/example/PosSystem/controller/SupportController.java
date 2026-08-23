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

    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostMapping("/ticket")
    public ResponseEntity<?> receiveSupportTicket(@RequestBody SupportRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(fromEmail);
            helper.setReplyTo(request.getEmail());
            helper.setSubject("App Support Request from " + request.getName());
            helper.setText("User: " + request.getName() + "\nEmail: " + request.getEmail() + "\n\nIssue Details:\n" + request.getQuery());

            if (request.getScreenshotBase64() != null && !request.getScreenshotBase64().trim().isEmpty()) {
                String cleanBase64 = request.getScreenshotBase64().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                ByteArrayResource resource = new ByteArrayResource(imageBytes) {
                    @Override
                    public String getFilename() {
                        return "screenshot.jpg";
                    }
                };
                helper.addAttachment(resource.getFilename(), resource);
            }

            mailSender.send(message);
            return ResponseEntity.ok(Map.of("message", "Ticket submitted successfully"));
        } catch (Exception e) {
            System.err.println("Support ticket email sending failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send email: " + e.getMessage()));
        }
    }
}