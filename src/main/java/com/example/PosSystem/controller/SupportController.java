package com.example.PosSystem.controller;

import com.example.PosSystem.Model.SupportRequest;
import com.example.PosSystem.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SupportController {

    private final EmailService emailService;

    @PostMapping("/ticket")
    public ResponseEntity<?> receiveSupportTicket(@RequestBody SupportRequest request) {
        try {
            // Call our new Brevo API method
            emailService.sendSupportEmail(
                    request.getName(),
                    request.getEmail(),
                    request.getQuery(),
                    request.getScreenshotBase64()
            );

            return ResponseEntity.ok(Map.of("message", "Ticket submitted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send email: " + e.getMessage()));
        }
    }
}