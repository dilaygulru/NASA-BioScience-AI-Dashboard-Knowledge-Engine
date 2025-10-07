package com.example.astrogenesis.config;

import com.example.astrogenesis.entity.Publication;
import com.example.astrogenesis.repository.PublicationRepository;
import com.example.astrogenesis.service.PublicationLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DatabaseInitializer {

    private final PublicationLoader loader;
    private final PublicationRepository repository;

    public DatabaseInitializer(PublicationLoader loader, PublicationRepository repository) {
        this.loader = loader;
        this.repository = repository;
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (repository.count() > 0) {
                System.out.println("ℹ️ Database already contains data, skipping initialization.");
                return;
            }

            try {
                System.out.println("🚀 Loading NASA publication data from CSV...");
                List<Publication> publications = loader.loadFromCsv();

                for (Publication pub : publications) {
                    // 🧠 Ek alanları doldur
                    if (pub.getPublicationDate() == null) {
                        pub.setPublicationDate(LocalDate.now());

                    }
                    if (pub.getSource() == null) {
                        pub.setSource("PubMed Central");
                    }
                    if (pub.getFetchedAt() == null) {
                        pub.setFetchedAt(LocalDateTime.now());
                    }

                    // Eğer DOI, Keywords, Topics yoksa null kalsın — ileride LLM dolduracak
                }

                repository.saveAll(publications);
                System.out.println("✅ Loaded " + publications.size() + " enriched publications into the database.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Failed to initialize database: " + e.getMessage());
            }
        };
    }
}
