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
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:pos1111.noreply@gmail.com}")
    private String fromEmail;

    @PostMapping("/ticket")
    public ResponseEntity<?> receiveSupportTicket(@RequestBody SupportRequest request) {
        // Send email asynchronously in the background so Android doesn't time out
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(fromEmail);
                helper.setTo("pos1111.noreply@gmail.com");
                helper.setReplyTo(request.getEmail());
                helper.setSubject("App Support Request from " + request.getName());
                helper.setText("User: " + request.getName() + "\nEmail: " + request.getEmail() + "\n\nIssue Details:\n" + request.getQuery());

                if (request.getScreenshotBase64() != null && !request.getScreenshotBase64().trim().isEmpty()) {
                    String cleanBase64 = request.getScreenshotBase64().replaceAll("\\s+", "");
                    byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
                    ByteArrayResource resource = new ByteArrayResource(imageBytes);
                    helper.addAttachment("screenshot.jpg", resource);
                }

                mailSender.send(message);
                System.out.println("Support email sent successfully to pos1111.noreply@gmail.com");
            } catch (Exception e) {
                System.err.println("Async email sending failed: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Immediately respond 200 OK to the Android app
        return ResponseEntity.ok(Map.of("message", "Ticket submitted successfully"));
    }
}