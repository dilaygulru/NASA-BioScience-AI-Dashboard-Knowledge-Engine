package com.example.astrogenesis.controller;

import com.example.astrogenesis.entity.ChatHistory;
import com.example.astrogenesis.repository.ChatHistoryRepository;
import com.example.astrogenesis.service.ChatHistoryPdfService;
import com.example.astrogenesis.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatHistoryPdfService chatHistoryPdfService;

    public ReportController(ReportService reportService,
                           ChatHistoryRepository chatHistoryRepository,
                           ChatHistoryPdfService chatHistoryPdfService) {
        this.reportService = reportService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatHistoryPdfService = chatHistoryPdfService;
    }

    @GetMapping
    public String reportPage() {
        return "report"; // AstroLog - reports history page
    }

    // 🔹 PDF generate (AI sayfasındaki “Generate PDF” butonu çağıracak)
    @GetMapping("/generate")
    public ResponseEntity<InputStreamResource> generateReport(@RequestParam String query) {
        InputStreamResource pdf = reportService.generatePdf(query);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 🔹 JSON endpoint for chat history
    @GetMapping("/history")
    @ResponseBody
    public List<Map<String, Object>> getChatHistory() {
        List<ChatHistory> histories = chatHistoryRepository.findAllByOrderByCreatedAtDesc();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return histories.stream().map(history -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", history.getId());
            map.put("query", history.getQuery());
            map.put("audience", history.getAudience());
            map.put("summary", history.getSummary());
            map.put("date", history.getCreatedAt().format(formatter));
            return map;
        }).collect(Collectors.toList());
    }

    // 🔹 Get detailed chat history by ID
    @GetMapping("/history/{id}")
    @ResponseBody
    public Map<String, Object> getChatHistoryDetails(@PathVariable Long id) {
        ChatHistory history = chatHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat history not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> result = new HashMap<>();
        result.put("id", history.getId());
        result.put("query", history.getQuery());
        result.put("audience", history.getAudience());
        result.put("summary", history.getSummary());
        result.put("keyFindings", history.getKeyFindings());
        result.put("charts", history.getCharts());
        result.put("tables", history.getTables());
        result.put("recommendations", history.getRecommendations());
        result.put("sources", history.getSources());
        result.put("createdAt", history.getCreatedAt().format(formatter));

        return result;
    }

    // 🔹 Download PDF for specific chat history
    @GetMapping("/history/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadChatHistoryPdf(@PathVariable Long id) {
        try {
            ChatHistory history = chatHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Chat history not found"));

            byte[] pdfBytes = chatHistoryPdfService.generatePdf(history);

            // Create filename from query (sanitized)
            String filename = history.getQuery()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "_")
                    .substring(0, Math.min(50, history.getQuery().length()))
                    + "_report.pdf";

            ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(new InputStreamResource(bis));

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🔹 Delete specific chat history
    @DeleteMapping("/history/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteChatHistory(@PathVariable Long id) {
        try {
            if (chatHistoryRepository.existsById(id)) {
                chatHistoryRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Chat history deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Chat history not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting chat history: " + e.getMessage()));
        }
    }

    // 🔹 Delete ALL chat history
    @DeleteMapping("/history/all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteAllChatHistory() {
        try {
            long count = chatHistoryRepository.count();
            chatHistoryRepository.deleteAll();
            return ResponseEntity.ok(Map.of(
                "message", "All chat history deleted successfully",
                "count", String.valueOf(count)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error deleting chat history: " + e.getMessage()));
        }
    }
}
