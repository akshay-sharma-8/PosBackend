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

    @Value("${spring.mail.username:pos111.noreply@gmail.com}")
    private String fromEmail;

    @PostMapping("/ticket")
    public ResponseEntity<?> receiveSupportTicket(@RequestBody SupportRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo("pos111.noreply@gmail.com");
            helper.setReplyTo(request.getEmail());
            helper.setSubject("App Support Request from " + request.getName());
            helper.setText("User: " + request.getName() + "\nEmail: " + request.getEmail() + "\n\nIssue Details:\n" + request.getQuery());

            if (request.getScreenshotBase64() != null && !request.getScreenshotBase64().trim().isEmpty()) {
                String cleanBase64 = request.getScreenshotBase64().replaceAll("\\s+", "");
                byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

                // Fixed ByteArrayResource to ensure JavaMail Sender recognizes it as a valid file
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
            // This prints the error in your Railway Logs
            e.printStackTrace();
            // This sends the exact error back to your Android App
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}