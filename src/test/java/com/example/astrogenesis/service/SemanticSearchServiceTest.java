
package com.example.astrogenesis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SemanticSearchServiceTest {

    @Autowired
    private SemanticSearchService semanticSearchService;

    @Test
    public void testFindRelevantTexts() {
        // 🚀 1. Arama sorgusu belirle
        String query = "microgravity bone loss";

        // 🚀 2. Semantic arama çalıştır
        List<String> results = semanticSearchService.findRelevantTexts(query, 5);

        // 🚀 3. Çıktıyı kontrol et
        System.out.println("🔎 Query: " + query);
        System.out.println("📊 Found " + results.size() + " relevant records:");
        for (String text : results) {
            System.out.println(text);
        }

        // 🚀 4. Basit doğrulama (manuel)
        assert results != null && !results.isEmpty() : "No relevant records found — embeddings may not be used!";
    }
}
