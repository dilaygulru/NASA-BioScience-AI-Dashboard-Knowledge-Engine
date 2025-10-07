package com.example.astrogenesis.config;

import com.example.astrogenesis.service.PublicationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingInitializer implements CommandLineRunner {

    private final PublicationService publicationService;

    public EmbeddingInitializer(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public void run(String... args) {
        System.out.println("🧠 Generating missing embeddings...");
        publicationService.generateMissingEmbeddings();
        System.out.println("✅ Embedding generation complete!");
    }
}
