package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.OSDRDataset;
import com.example.astrogenesis.repository.OSDRDatasetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class EmbeddingService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private static final String API_URL =
            "https://api-inference.huggingface.co/models/BAAI/bge-small-en-v1.5";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final OSDRDatasetRepository datasetRepository;

    public EmbeddingService(OSDRDatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    /**
     * Tek bir metin için embedding oluşturur
     */
    public String generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            System.err.println("⚠️ Empty text, skipping embedding generation.");
            return null;
        }

        try {
            // 🔹 Metni temizle
            String cleanText = text
                    .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ") // kontrol karakterlerini sil
                    .replaceAll("\"", "'")                      // çift tırnakları düzelt
                    .replaceAll("\\s+", " ")                    // fazla boşlukları azalt
                    .trim();

            // 🔹 Hugging Face API karakter sınırını aşma
            if (cleanText.length() > 9000) {
                cleanText = cleanText.substring(0, 9000);
            }

            // 🔹 JSON body
            String requestBody = "{\"inputs\": \"" + cleanText + "\"}";

            // 🔹 HTTP isteği oluştur
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ HuggingFace API failed (" + response.statusCode() + "): " + response.body());
                return null;
            }

            JsonNode json = objectMapper.readTree(response.body());
            if (json.isArray() && json.size() == 1 && json.get(0).isArray()) {
                json = json.get(0);
            }

            return json.toString();

        } catch (Exception e) {
            System.err.println("❌ Error generating embedding: " + e.getMessage());
            return null;
        }
    }

    /**
     * OSDR tablosundaki embedding’i boş olan kayıtlar için embedding oluşturur.
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

                String embedding = generateEmbedding(textForEmbedding);
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
        System.out.println("✅ Embedding generation complete!");
    }
}
