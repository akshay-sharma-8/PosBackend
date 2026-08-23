package com.example.PosSystem.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class EmailService {

    // Safely reads the key from Railway
    @Value("${BREVO_API_KEY}")
    private String apiKey;

    // Ensure this matches your Brevo login email
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

    // NEW METHOD: Handles Support Tickets with Base64 Image Attachments
    public void sendSupportEmail(String userName, String userEmail, String query, String base64Image) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("accept", "application/json");

        String htmlContent = "<h2>New Support Ticket</h2>"
                + "<p><strong>User:</strong> " + userName + "</p>"
                + "<p><strong>Email:</strong> " + userEmail + "</p>"
                + "<hr><p><strong>Issue Details:</strong></p>"
                + "<p>" + query + "</p>";

        // Use HashMap so we can optionally add the attachment later
        Map<String, Object> body = new HashMap<>();

        // Brevo requires the sender to be your verified email
        body.put("sender", Map.of("name", userName + " (Via App)", "email", fromEmail));

        // Send the support ticket TO YOURSELF so you can read it
        body.put("to", List.of(Map.of("email", fromEmail)));

        // Sets the Reply-To to the user, so if you click 'Reply' in Gmail, it goes to them
        body.put("replyTo", Map.of("email", userEmail, "name", userName));
        body.put("subject", "App Support Request from " + userName);
        body.put("htmlContent", htmlContent);

        // Attach the screenshot if the user provided one
        if (base64Image != null && !base64Image.trim().isEmpty()) {
            String cleanBase64 = base64Image.replaceAll("\\s+", "");
            body.put("attachment", List.of(
                    Map.of(
                            "name", "screenshot.jpg",
                            "content", cleanBase64
                    )
            ));
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Support ticket successfully sent via Brevo API!");
        } catch (Exception e) {
            System.err.println("Failed to send support ticket via Brevo API: " + e.getMessage());
            throw new RuntimeException("Email API failed");
        }
    }
}