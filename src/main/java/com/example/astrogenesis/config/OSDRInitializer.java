package com.example.astrogenesis.config;

import com.example.astrogenesis.service.OSDRIngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OSDRInitializer implements CommandLineRunner {

    private final OSDRIngestionService ingestionService;

    public OSDRInitializer(OSDRIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(String... args) {
        ingestionService.fetchNewBiologicalData();
    }
}
