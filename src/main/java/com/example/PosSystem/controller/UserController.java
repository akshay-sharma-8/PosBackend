package com.example.PosSystem.controller;

import com.example.PosSystem.Model.User;
import com.example.PosSystem.repository.UserRepository;
import com.example.PosSystem.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final EmailService emailService; // Email Service is restored!

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        if (userRepository.existsByUsername(user.getUsername())) {
            response.put("message", "Username already exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        userRepository.save(user);
        response.put("message", "Registration successful!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        Optional<User> userOptional = userRepository.findByUsername(username);
        Map<String, String> response = new HashMap<>();
        if (userOptional.isPresent() && userOptional.get().getPassword().equals(password)) {
            response.put("message", "Login successful");
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid username or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserProfile(@PathVariable String username) {
        return userRepository.findByUsername(username).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUserProfile(@PathVariable String username, @RequestBody User updatedUser) {
        return userRepository.findByUsername(username).map(user -> {
            user.setPhone(updatedUser.getPhone());
            user.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) user.setPassword(updatedUser.getPassword());
            if (updatedUser.getProfilePicture() != null) user.setProfilePicture(updatedUser.getProfilePicture());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Optional<User> userOptional = userRepository.findByUsername(username);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                response.put("message", "No email registered to this account.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Generate a random 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            user.setResetOtp(otp);
            userRepository.save(user);

            // --- RAILWAY FIREWALL BYPASS ---
            // Railway blocks Gmail, so we print the OTP to the console instead!
            System.out.println("\n===============================================");
            System.out.println("PASSWORD RESET REQUESTED FOR USER: " + username);
            System.out.println("THE 6-DIGIT OTP IS: " + otp);
            System.out.println("===============================================\n");

            response.put("message", "OTP generated! Check Railway logs to see it.");
            return ResponseEntity.ok(response);

        } else {
            response.put("message", "Username not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        Optional<User> userOptional = userRepository.findByUsername(username);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getResetOtp() != null && user.getResetOtp().equals(otp)) {
                user.setPassword(newPassword);
                user.setResetOtp(null); // Clear OTP after use
                userRepository.save(user);
                response.put("message", "Password successfully updated.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid OTP.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}