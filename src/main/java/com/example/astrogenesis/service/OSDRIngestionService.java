package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.OSDRDataset;
import com.example.astrogenesis.repository.OSDRDatasetRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class OSDRIngestionService {

    private static final String BASE_URL =
            "https://osdr.nasa.gov/geode-py/ws/repo/search?source=cgene,alsda,esa&type=study&sort=Study%20Public%20Release%20Date&order=desc";

    @Autowired
    private OSDRDatasetRepository repository;

    @Autowired
    private OSDREmbeddingService osdrEmbeddingService; // ✅ Embedding servisini ekledik

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void fetchNewBiologicalData() {
        System.out.println("🛰 Fetching New Biological Data from NASA OSDR...");

        int from = 0;
        int size = 100;
        int totalAdded = 0;

        try {
            while (true) {
                String url = String.format("%s&from=%d&size=%d", BASE_URL, from, size);
                System.out.printf("➡️ Fetching page starting at %d...%n", from);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.err.println("❌ Failed to fetch page " + from + ": HTTP " + response.statusCode());
                    break;
                }

                JsonNode root = objectMapper.readTree(response.body());
                JsonNode hits = root.path("hits").path("hits");

                if (!hits.isArray() || hits.isEmpty()) {
                    System.out.println("✅ No more data available, stopping.");
                    break;
                }

                int addedCount = 0;

                for (JsonNode hit : hits) {
                    JsonNode source = hit.path("_source");

                    String name = source.path("Study Title").asText(null);
                    if (name == null || name.isBlank()) continue;

                    // Duplicate kontrolü
                    List<OSDRDataset> existing = repository.findByName(name);
                    if (existing != null && !existing.isEmpty()) continue;

                    OSDRDataset dataset = new OSDRDataset();
                    dataset.setName(name);
                    dataset.setDescription(source.path("Study Protocol Description").asText(null));
                    dataset.setCategory(source.path("Flight Program").asText("Biological"));
                    dataset.setLink("https://osdr.nasa.gov/bio/repo/data/" +
                            source.path("Authoritative Source URL").asText(""));
                    dataset.setDoi(source.path("Data Source Accession").asText(null));

                    long timestamp = source.path("Study Public Release Date").asLong(0);
                    if (timestamp > 0) {
                        dataset.setExperimentDate(LocalDate.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC));
                    }

                    dataset.setFetchedAt(LocalDateTime.now());
                    repository.save(dataset);
                    addedCount++;
                }

                totalAdded += addedCount;
                System.out.printf("📦 Page %d → %d new records added. (Total so far: %d)%n",
                        from / size + 1, addedCount, totalAdded);

                if (hits.size() < size) {
                    System.out.println("✅ Reached last page.");
                    break;
                }

                from += size;
                Thread.sleep(500);
            }

            System.out.printf("🎯 Total new datasets added: %d%n", totalAdded);
            System.out.println("✅ OSDR data ingestion complete!");

            // 🔹 Yeni eklenen veya eksik embedding’leri üret
            System.out.println("🧠 Generating missing embeddings for OSDR datasets...");
            osdrEmbeddingService.generateMissingEmbeddings(); // ✅ burası eklendi

        } catch (Exception e) {
            System.err.println("❌ Failed to fetch OSDR data: " + e.getMessage());
        }
    }
}
