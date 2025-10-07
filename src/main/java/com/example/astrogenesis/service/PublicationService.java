package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.Publication;
import com.example.astrogenesis.repository.PublicationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PublicationService {

    private final PublicationRepository repository;
    private final EmbeddingService embeddingService;

    public PublicationService(PublicationRepository repository, EmbeddingService embeddingService) {
        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public List<Publication> getAllPublications() {
        return repository.findAll();
    }

    public Publication savePublication(Publication publication) {
        return repository.save(publication);
    }

    public List<Publication> searchPublications(String query) {
        return repository.searchPublications(query);
    }

    // 🧠 Eksik embedding'leri oluştur ve DB'ye kaydet
    public void generateMissingEmbeddings() {
        List<Publication> publications = repository.findAll();
        int count = 0;

        for (Publication pub : publications) {
            try {
                if (pub.getEmbeddingVector() == null || pub.getEmbeddingVector().isBlank()) {
                    String sourceText = pub.getSummary() != null && !pub.getSummary().isBlank()
                            ? pub.getSummary()
                            : pub.getTitle();

                    String embedding = embeddingService.generateEmbedding(sourceText);

                    if (embedding != null) {
                        pub.setEmbeddingVector(embedding);
                        repository.save(pub);
                        count++;
                        System.out.println("✅ Embedded: " + pub.getTitle());
                    } else {
                        System.err.println("⚠️ Failed to generate embedding for: " + pub.getTitle());
                    }

                    Thread.sleep(1000); // API hız limiti için küçük gecikme
                }

            } catch (Exception e) {
                System.err.println("❌ Error embedding " + pub.getTitle() + ": " + e.getMessage());
            }
        }

        System.out.println("🎯 Total embeddings generated: " + count);
    }
}
