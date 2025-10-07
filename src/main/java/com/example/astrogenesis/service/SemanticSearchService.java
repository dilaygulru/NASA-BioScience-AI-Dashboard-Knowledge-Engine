package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.OSDRDataset;
import com.example.astrogenesis.entity.Publication;
import com.example.astrogenesis.repository.OSDRDatasetRepository;
import com.example.astrogenesis.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private final PublicationRepository publicationRepository;
    private final OSDRDatasetRepository osdrDatasetRepository;
    private final EmbeddingService embeddingService;

    public SemanticSearchService(
            PublicationRepository publicationRepository,
            OSDRDatasetRepository osdrDatasetRepository,
            EmbeddingService embeddingService
    ) {
        this.publicationRepository = publicationRepository;
        this.osdrDatasetRepository = osdrDatasetRepository;
        this.embeddingService = embeddingService;
    }

    /**
     * Kullanıcı sorgusuna göre en benzer metinleri bulur (RAG için)
     */
    public List<String> findRelevantTexts(String query, int limit) {
        System.out.println("🔎 Running semantic search for: " + query);

        String queryEmbeddingJson = embeddingService.generateEmbedding(query);
        if (queryEmbeddingJson == null) {
            System.err.println("⚠️ Query embedding could not be generated.");
            return Collections.emptyList();
        }

        List<Double> queryEmbedding = parseEmbedding(queryEmbeddingJson);

        // 1️⃣ Tüm kayıtları çek
        List<Publication> publications = publicationRepository.findAll();
        List<OSDRDataset> datasets = osdrDatasetRepository.findAll();

        List<ScoredText> allTexts = new ArrayList<>();

        // 2️⃣ Publication skorlarını hesapla
        for (Publication pub : publications) {
            if (pub.getEmbeddingVector() == null) continue;

            List<Double> emb = parseEmbedding(pub.getEmbeddingVector());
            double score = cosineSimilarity(queryEmbedding, emb);

            String content = (pub.getSummary() != null && !pub.getSummary().isBlank())
                    ? pub.getSummary()
                    : pub.getContent();

            if (content != null && !content.isBlank()) {
                allTexts.add(new ScoredText(
                        pub.getTitle(),
                        content,
                        score,
                        "Publication",
                        pub.getLink(),
                        pub.getDoi(),
                        null, // OSDR için null
                        pub.getImageUrls() // Image URLs
                ));
            }
        }

        // 3️⃣ OSDR Dataset skorlarını hesapla
        for (OSDRDataset ds : datasets) {
            if (ds.getEmbeddingVector() == null) continue;

            List<Double> emb = parseEmbedding(ds.getEmbeddingVector());
            double score = cosineSimilarity(queryEmbedding, emb);

            if (ds.getDescription() != null && !ds.getDescription().isBlank()) {
                allTexts.add(new ScoredText(
                        ds.getName(),
                        ds.getDescription(),
                        score,
                        "OSDR",
                        ds.getLink(), // OSDR link
                        ds.getDoi(), // OSDR DOI
                        ds.getName(), // OSDR ID (name field contains the OSDR ID like "OSD-123")
                        null // OSDR datasets don't have images in this implementation
                ));
            }
        }

        // 4️⃣ En yüksek skorları sırala
        return allTexts.stream()
                .sorted(Comparator.comparingDouble(ScoredText::score).reversed())
                .limit(limit)
                .map(t -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(t.source).append("] ").append(t.title).append(" ||| ");
                    sb.append(t.text);

                    // Add metadata
                    if (t.link != null && !t.link.isBlank()) {
                        sb.append(" ||| LINK:").append(t.link);
                    }
                    if (t.doi != null && !t.doi.isBlank()) {
                        sb.append(" ||| DOI:").append(t.doi);
                    }
                    if (t.osdrId != null && !t.osdrId.isBlank()) {
                        sb.append(" ||| OSDR_ID:").append(t.osdrId);
                    }
                    if (t.imageUrls != null && !t.imageUrls.isBlank()) {
                        sb.append(" ||| IMAGES:").append(t.imageUrls);
                    }

                    return sb.toString();
                })
                .collect(Collectors.toList());
    }

    // --- 🔧 Yardımcı metodlar ---

    private List<Double> parseEmbedding(String json) {
        try {
            json = json.replace("[", "").replace("]", "");
            String[] parts = json.split(",");
            List<Double> values = new ArrayList<>();
            for (String p : parts) {
                values.add(Double.parseDouble(p.trim()));
            }
            return values;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.isEmpty() || v2.isEmpty() || v1.size() != v2.size()) return 0.0;

        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2) + 1e-8);
    }

    private record ScoredText(String title, String text, double score, String source,
                              String link, String doi, String osdrId, String imageUrls) {}
}
