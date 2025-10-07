package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.OSDRDataset;
import com.example.astrogenesis.repository.OSDRDatasetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OSDREmbeddingService {

    private final OSDRDatasetRepository datasetRepository;
    private final EmbeddingService embeddingService; // Hugging Face ile çalışan servis

    public OSDREmbeddingService(OSDRDatasetRepository datasetRepository,
                                EmbeddingService embeddingService) {
        this.datasetRepository = datasetRepository;
        this.embeddingService = embeddingService;
    }

    /**
     * Veritabanında embedding'i eksik olan OSDR datasetleri için embedding oluşturur.
     */
    public void generateMissingEmbeddings() {
        System.out.println("🧠 Checking OSDR datasets for missing embeddings...");

        List<OSDRDataset> datasets = datasetRepository.findAll();
        int total = datasets.size();
        int updated = 0;

        for (OSDRDataset dataset : datasets) {
            try {
                if (dataset.getEmbeddingVector() != null && !dataset.getEmbeddingVector().isBlank())
                    continue;

                String textForEmbedding = dataset.getDescription();
                if (textForEmbedding == null || textForEmbedding.isBlank()) {
                    textForEmbedding = dataset.getName();
                }

                String embedding = embeddingService.generateEmbedding(textForEmbedding);
                if (embedding != null) {
                    dataset.setEmbeddingVector(embedding);
                    datasetRepository.save(dataset);
                    updated++;
                }

                System.out.printf("✅ [%d/%d] Embedded: %s%n", updated, total, dataset.getName());

                // Küçük bekleme (rate limit koruması)
                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("⚠️ Failed to generate embedding for: " + dataset.getName());
                e.printStackTrace();
            }
        }

        System.out.printf("🎯 Total embeddings generated: %d / %d%n", updated, total);
        System.out.println("✅ OSDR embedding generation complete!");
    }
}
