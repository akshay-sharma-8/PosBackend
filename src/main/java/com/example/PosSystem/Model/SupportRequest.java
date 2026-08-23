package com.example.PosSystem.Model;


public class SupportRequest {
    private String name;
    private String email;
    private String query;
    private String screenshotBase64;

    // Constructors
    public SupportRequest() {}

    public SupportRequest(String name, String email, String query, String screenshotBase64) {
        this.name = name;
        this.email = email;
        this.query = query;
        this.screenshotBase64 = screenshotBase64;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getScreenshotBase64() { return screenshotBase64; }
    public void setScreenshotBase64(String screenshotBase64) { this.screenshotBase64 = screenshotBase64; }
}