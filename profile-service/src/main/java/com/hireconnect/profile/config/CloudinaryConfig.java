package com.hireconnect.profile.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String cloudUrl = String.format("cloudinary://%s:%s@%s", // Special key, which Clodinary use
                apiKey, apiSecret, cloudName.trim().toLowerCase());
        System.out.println("=== [CloudinaryConfig] Connecting with cloud_name: " + cloudName.trim().toLowerCase() + " ===");
        return new Cloudinary(cloudUrl);
    }
}
