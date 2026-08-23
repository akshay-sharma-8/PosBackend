package com.example.PosSystem.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import java.util.Map;
import java.util.List;

@Service
public class EmailService {

    // Safely reads the key from Railway
    @Value("${BREVO_API_KEY}")
    private String apiKey;

    // Make sure this matches your Brevo account email
    private final String fromEmail = "pos111.noreply@gmail.com";

    public void sendOtpEmail(String toEmail, String otp) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("accept", "application/json");

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Smart Shop Support", "email", fromEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Smart Shop - Password Reset OTP",
                "htmlContent", "<h2>Smart Shop Password Reset</h2><p>Hello,</p><p>Your 6-digit OTP for password reset is: <b style='font-size: 20px;'>" + otp + "</b></p><p>If you did not request this, please ignore this email.</p>"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("OTP Email successfully delivered to " + toEmail + " via Brevo API!");
        } catch (Exception e) {
            System.err.println("Brevo API delivery failed: " + e.getMessage());
            throw new RuntimeException("Failed to send email via Brevo API: " + e.getMessage());
        }
    }
}