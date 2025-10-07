package com.example.astrogenesis.controller;

import com.example.astrogenesis.entity.ChatHistory;
import com.example.astrogenesis.repository.ChatHistoryRepository;
import com.example.astrogenesis.service.LLMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ai")
public class AIController {

    private final LLMService llmService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ObjectMapper objectMapper;

    public AIController(LLMService llmService, ChatHistoryRepository chatHistoryRepository) {
        this.llmService = llmService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public String aiChat() {
        return "ai-summary"; // AstroChat - simple chat interface
    }

    @PostMapping("/chat")
    @ResponseBody
    public Map<String, Object> chatWithAI(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        String audience = payload.getOrDefault("audience", "general");

        if (query == null || query.isBlank()) {
            return Map.of("summary", "⚠️ Please provide a valid question.");
        }

        System.out.println("📝 [AI Controller] Query: " + query);
        System.out.println("👥 [AI Controller] Audience: " + audience);

        // 🚀 RAG + audience-specific analysis + visualization
        Map<String, Object> report = llmService.generateAnalyticalReport(query, audience);

        System.out.println("📊 [AI Controller] Report keys: " + report.keySet());

        // Eğer model sadece text döndüyse fallback
        if (!report.containsKey("summary")) {
            return Map.of("summary", report);
        }

        // 💾 Save chat history to database
        try {
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setQuery(query);
            chatHistory.setAudience(audience);
            chatHistory.setSummary(report.get("summary") != null ? report.get("summary").toString() : "");
            chatHistory.setKeyFindings(objectMapper.writeValueAsString(report.get("keyFindings")));
            chatHistory.setCharts(objectMapper.writeValueAsString(report.get("charts")));
            chatHistory.setTables(objectMapper.writeValueAsString(report.get("tables")));
            chatHistory.setRecommendations(objectMapper.writeValueAsString(report.get("recommendations")));
            chatHistory.setSources(objectMapper.writeValueAsString(report.get("sourcesMetadata")));

            chatHistoryRepository.save(chatHistory);
            System.out.println("✅ [AI Controller] Chat history saved successfully");
        } catch (Exception e) {
            System.err.println("❌ [AI Controller] Failed to save chat history: " + e.getMessage());
        }

        return report;
    }

    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> testAI() {
        System.out.println("🧪 [TEST] Running simple AI test...");

        // Simple test query
        Map<String, Object> result = llmService.generateAnalyticalReport(
            "What are the effects of microgravity on bone density?",
            "general"
        );

        return Map.of(
            "status", "test completed",
            "hasCharts", result.containsKey("charts"),
            "hasTables", result.containsKey("tables"),
            "hasKeyFindings", result.containsKey("keyFindings"),
            "keys", result.keySet().toString(),
            "chartCount", result.containsKey("charts") ? ((List) result.get("charts")).size() : 0,
            "tableCount", result.containsKey("tables") ? ((List) result.get("tables")).size() : 0
        );
    }
}
