package com.example.astrogenesis.config;

import com.example.astrogenesis.service.OSDREmbeddingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OSDREmbeddingInitializer implements CommandLineRunner {

    private final OSDREmbeddingService osdrEmbeddingService;

    public OSDREmbeddingInitializer(OSDREmbeddingService osdrEmbeddingService) {
        this.osdrEmbeddingService = osdrEmbeddingService;
    }

    @Override
    public void run(String... args) {
        System.out.println("🚀 Starting OSDR embedding generation...");
        osdrEmbeddingService.generateMissingEmbeddings();
    }
}
