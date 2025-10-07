package com.example.astrogenesis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LLMService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final SemanticSearchService semanticSearchService;
    private final PromptHandler promptHandler;

    public LLMService(SemanticSearchService semanticSearchService, PromptHandler promptHandler) {
        this.semanticSearchService = semanticSearchService;
        this.promptHandler = promptHandler;

        // Configure RestTemplate with timeout
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        });
    }

    /**
     * 🧠 Generate analytical report (Semantic + Context + Chart/Table JSON)
     * Enhanced for NASA Challenge: Scientists, Managers, Mission Architects
     */
    public Map<String, Object> generateAnalyticalReport(String query) {
        return generateAnalyticalReport(query, "general");
    }

    /**
     * 🧠 Generate analytical report with audience-specific focus
     * @param query User's research question
     * @param audience "scientist", "manager", "mission_architect", or "general"
     */
    public Map<String, Object> generateAnalyticalReport(String query, String audience) {
        System.out.println("🧠 [RAG] Generating analytical report for: " + query + " (Audience: " + audience + ")");

        // 1️⃣ Pre-filter simple/greeting/off-topic queries
        Map<String, Object> preResponse = promptHandler.handlePrePrompt(query);
        if (preResponse != null) {
            // If PromptHandler returned something, it means we should stop here
            return preResponse;
        }
        // If preResponse is null → proceed to RAG

        // 2️⃣ Semantic search to collect context
        List<String> relevantTexts = semanticSearchService.findRelevantTexts(query, 5);
        StringBuilder context = new StringBuilder();
        List<Map<String, String>> sourcesMetadata = new ArrayList<>();

        if (relevantTexts != null && !relevantTexts.isEmpty()) {
            for (String text : relevantTexts) {
                String[] parts = text.split("\\|\\|\\|");
                String title = parts.length > 0 ? parts[0].trim() : "Untitled";
                String summary = parts.length > 1 ? parts[1].trim() : "";

                // Extract metadata
                String link = null;
                String doi = null;
                String osdrId = null;
                String imageUrls = null;

                for (int i = 2; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (part.startsWith("LINK:")) {
                        link = part.substring(5).trim();
                    } else if (part.startsWith("DOI:")) {
                        doi = part.substring(4).trim();
                    } else if (part.startsWith("OSDR_ID:")) {
                        osdrId = part.substring(8).trim();
                    } else if (part.startsWith("IMAGES:")) {
                        imageUrls = part.substring(7).trim();
                    }
                }

                // Build source metadata
                Map<String, String> source = new HashMap<>();
                source.put("title", title);
                if (link != null) source.put("link", link);
                if (doi != null) source.put("doi", doi);
                if (osdrId != null) source.put("osdrId", osdrId);
                if (imageUrls != null) source.put("imageUrls", imageUrls);
                sourcesMetadata.add(source);

                // Build context for LLM
                context.append("📘 ").append(title).append("\n").append(summary);
                if (link != null) context.append("\n   🔗 Link: ").append(link);
                if (doi != null) context.append("\n   📄 DOI: ").append(doi);
                if (osdrId != null) context.append("\n   🛰️ OSDR: ").append(osdrId);
                if (imageUrls != null) context.append("\n   🖼️ Images: ").append(imageUrls);
                context.append("\n\n");
            }
        } else {
            System.out.println("⚠️ No relevant documents found. Proceeding with plain LLM summarization.");
        }

        // Trim context to avoid token overflow
        String contextText = context.length() > 9000
                ? context.substring(0, 9000) + "\n...(context truncated)"
                : context.toString();

        // 3️⃣ Enhanced System prompt - NASA Challenge Requirements
        String audienceInstructions = getAudienceSpecificInstructions(audience);

        String systemPrompt = """
                You are an expert NASA bioscience data analyst supporting the Biological and Physical Sciences Division.
                Your mission: Analyze 608+ NASA bioscience publications to enable safe Moon and Mars exploration.

                TARGET AUDIENCE: %s

                CORE OBJECTIVES (NASA Challenge):
                1. Summarize research progress in space biology experiments
                2. Identify knowledge gaps requiring additional research
                3. Detect areas of scientific consensus or disagreement
                4. Provide actionable insights for mission planning
                5. Support hypothesis generation for new experiments

                ANALYSIS FRAMEWORK:
                • Scientific Progress: What have we definitively learned?
                • Knowledge Gaps: What critical questions remain unanswered?
                • Consensus/Disagreement: Where do studies agree or conflict?
                • Actionable Intelligence: What can mission planners/scientists DO with this?
                • Future Directions: What experiments are needed next?

                PUBLICATION SECTION ANALYSIS:
                • Results sections → Objective, demonstrated facts
                • Introduction sections → Research context and motivation
                • Conclusion sections → Forward-looking insights and implications

                Respond strictly in JSON format with these keys:
                {
                  "summary": "DETAILED multi-paragraph synthesis (4-5 paragraphs, 350-400 words) in HTML format. Structure as follows:

                  <h2>Research Context</h2>
                  <p>Provide comprehensive background on the topic, explaining why this research area is critical for space exploration. Reference the scope of available studies.</p>

                  <h2>Key Findings</h2>
                  <p>Integrate findings from ALL provided sources. Present specific data points, percentages, experimental conditions, and quantitative results. Compare and contrast findings across different studies. Identify patterns and trends. Use <strong>bold</strong> for emphasis and bullet points where appropriate:</p>
                  <ul>
                    <li>Finding 1 with data</li>
                    <li>Finding 2 with metrics</li>
                  </ul>

                  <h2>Implications & Significance</h2>
                  <p>Discuss what these findings mean for astronaut health, mission planning, and future research. Address the 'so what?' factor with concrete examples.</p>

                  <h2>Future Directions</h2>
                  <p>Connect findings to broader space biology principles and outline critical next steps.</p>

                  Write in a narrative style that tells the story of the research. Use proper HTML formatting including <h2> headers, <p> paragraphs, <strong>bold</strong>, <em>italic</em>, <ul><li> bullet points, and <ol><li> numbered lists where appropriate. Be specific, cite data, and synthesize insights rather than listing facts.",

                  "scientificProgress": {
                    "achievements": ["Breakthrough 1 with evidence", "Discovery 2 with metrics"],
                    "timeline": "Brief timeline of key developments",
                    "impactLevel": "high|medium|emerging"
                  },

                  "knowledgeGaps": {
                    "criticalGaps": ["Gap 1: What's missing and why it matters", "Gap 2: Unanswered question"],
                    "researchPriority": "high|medium|low",
                    "suggestedStudies": ["Proposed experiment 1", "Proposed experiment 2"]
                  },

                  "consensus": {
                    "agreements": ["Area of consensus 1", "Agreed finding 2"],
                    "disagreements": ["Conflicting evidence about X", "Debate over Y"],
                    "confidenceLevel": "strong|moderate|limited"
                  },

                  "keyFindings": [
                    "Finding 1 with specific quantitative data and source",
                    "Finding 2 with experimental conditions and results",
                    "Finding 3 with implications for human spaceflight"
                  ],

                  "missionRelevance": {
                    "applicable": true,
                    "moonMissions": "How this affects lunar exploration (ONLY if relevant to Moon missions)",
                    "marsMissions": "How this affects Mars missions (ONLY if relevant to Mars missions)",
                    "riskFactors": ["Specific risk 1 for astronauts/missions", "Risk 2"],
                    "mitigationStrategies": ["Evidence-based strategy 1", "Strategy 2"]
                  },

                  "statistics": {
                    "totalStudies": 0,
                    "dateRange": "YYYY-YYYY",
                    "organisms": ["organism1", "organism2"],
                    "experimentTypes": ["type1", "type2"],
                    "keyEnvironmentalFactors": ["microgravity", "radiation", "other"]
                  },

                  "charts": [
                    {
                      "type": "bar|line|pie|radar|scatter",
                      "title": "Descriptive chart title showing trends or comparisons",
                      "x": ["Label1", "Label2", "Label3"],
                      "y": [value1, value2, value3],
                      "backgroundColor": ["rgba(54,162,235,0.6)", "rgba(255,99,132,0.6)"],
                      "borderColor": ["rgba(54,162,235,1)", "rgba(255,99,132,1)"],
                      "insight": "What this chart reveals about the research"
                    }
                  ],

                  "tables": [
                    {
                      "title": "Comparison/summary table title",
                      "columns": ["Study/Factor", "Organism", "Condition", "Key Result", "Mission Impact"],
                      "rows": [["Value1", "Value2", "Value3", "Value4", "Value5"]],
                      "insight": "Key takeaway from this table"
                    }
                  ],

                  "recommendations": {
                    "forScientists": ["Hypothesis 1 to test", "Research direction 2"],
                    "forManagers": ["Investment opportunity 1", "Priority area 2"],
                    "forMissionPlanners": ["Operational consideration 1", "Design requirement 2"]
                  },

                  "citations": ["Brief reference to key studies mentioned, if identifiable from context"],

                  "sources": [
                    {
                      "title": "Study title from context",
                      "link": "URL if available",
                      "doi": "DOI if available",
                      "osdrId": "OSDR ID if available",
                      "relevance": "Brief explanation of how this source contributed to the analysis"
                    }
                  ]
                }

                CRITICAL REQUIREMENTS:
                • Summary MUST be 350-400 words, synthesizing ALL provided sources
                • Generate 2-3 meaningful, data-driven charts showing trends, comparisons, or distributions
                • Create 1-2 detailed comparison tables
                • All data must be derived from provided context
                • Focus on ACTIONABLE intelligence, not just summaries
                • Distinguish between proven facts (Results) and hypotheses (Conclusions)
                • Avoid speculation - flag where data is insufficient
                • Write the summary in a flowing narrative style, not bullet points
                • Connect insights across multiple studies to show the bigger picture

                MISSION RELEVANCE RULES:
                • Set "applicable" to false if the query is NOT directly related to space missions, astronaut health, or mission planning
                • Only include missionRelevance fields when the research has CLEAR implications for Moon/Mars exploration
                • Examples where missionRelevance IS applicable: bone loss, radiation effects, plant growth in space, crew health
                • Examples where missionRelevance is NOT applicable: general biology questions, Earth-based research, theoretical concepts
                • If applicable=false, you can omit moonMissions, marsMissions, riskFactors, and mitigationStrategies

                Return ONLY valid JSON. No markdown, no extra text.
                """.formatted(audienceInstructions);

        // 4️⃣ Build user prompt with context
        String userPrompt = """
                User query: %s

                Relevant NASA OSDR and publication data:
                %s

                CRITICAL INSTRUCTIONS FOR SUMMARY:
                1. Write a 350-400 word comprehensive synthesis across ALL %d sources provided above
                2. Structure in 4-5 detailed paragraphs following the format specified in the system prompt
                3. Include specific data points, percentages, and quantitative findings from each study
                4. Compare and contrast results across different studies
                5. Synthesize insights rather than listing individual study findings
                6. Use narrative storytelling style, not bullet points
                7. Connect the dots between different research areas

                IMPORTANT: You MUST respond with ONLY valid JSON. No markdown, no explanation, no extra text.
                Generate at least 2-3 charts with real data from the context above.
                Generate at least 1-2 tables comparing different studies.

                Start your response with { and end with }
                """.formatted(query, contextText, relevantTexts.size());

        // 5️⃣ Call LLM API
        long startTime = System.currentTimeMillis();
        String rawResponse = sendLLMRequest(systemPrompt, userPrompt);
        long endTime = System.currentTimeMillis();
        System.out.println("⏱️ LLM API call took: " + (endTime - startTime) + "ms");

        if (rawResponse == null) return Map.of("summary", "❌ No response from LLM.");

        // 6️⃣ Parse LLM JSON response
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Clean the response - handle newlines in JSON strings
            String cleaned = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Try to extract JSON if there's extra text
            int jsonStart = cleaned.indexOf("{");
            int jsonEnd = cleaned.lastIndexOf("}");

            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
            }

            // Fix newlines and special characters in JSON string values
            // This regex replaces unescaped newlines within quoted strings
            cleaned = cleaned.replaceAll("(?<!\\\\)\\n(?=[^\"]*\"(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "\\\\n")
                             .replaceAll("(?<!\\\\)\\r", "\\\\r")
                             .replaceAll("(?<!\\\\)\\t", "\\\\t");

            System.out.println("🔍 [DEBUG] Attempting to parse JSON response...");

            // Try parsing with relaxed settings
            try {
                Map<String, Object> result = mapper.readValue(cleaned, Map.class);
                System.out.println("✅ [DEBUG] JSON parsed successfully!");

                // Clean up any remaining newlines in summary if it exists
                if (result.containsKey("summary") && result.get("summary") != null) {
                    String summary = result.get("summary").toString();
                    // Normalize line breaks in HTML
                    summary = summary.replaceAll("\\\\n", " ")
                                   .replaceAll("\\s+", " ")
                                   .trim();
                    result.put("summary", summary);
                }

                // Validate required fields
                if (!result.containsKey("summary")) {
                    result.put("summary", "Analysis completed. See details below.");
                }

                // Add our collected sources metadata (in case LLM didn't include them)
                if (!sourcesMetadata.isEmpty()) {
                    result.put("sourcesMetadata", sourcesMetadata);
                    System.out.println("🖼️ [DEBUG] Adding sourcesMetadata to result: " + sourcesMetadata.size() + " sources");
                    int imageCount = 0;
                    for (Map<String, String> source : sourcesMetadata) {
                        if (source.containsKey("imageUrls") && source.get("imageUrls") != null && !source.get("imageUrls").isBlank()) {
                        System.out.println("   📸 Found images in source: " + source.get("title") + " -> " + source.get("imageUrls"));
                        imageCount++;
                    }
                }
                    System.out.println("   📊 Total sources with images: " + imageCount + " out of " + sourcesMetadata.size());
                } else {
                    System.out.println("⚠️ [DEBUG] No sourcesMetadata to add!");
                }

                return result;

            } catch (Exception innerEx) {
                System.err.println("⚠️ Failed to parse even after cleaning. Error: " + innerEx.getMessage());
                throw innerEx;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not parse JSON from LLM response.");
            System.err.println("Raw response: " + rawResponse.substring(0, Math.min(500, rawResponse.length())));
            e.printStackTrace();

            // Return a basic structure with the raw response
            return Map.of(
                "summary", rawResponse.replaceAll("[\\r\\n]+", " ").substring(0, Math.min(1000, rawResponse.length())),
                "keyFindings", List.of("Unable to parse structured response. Please try again."),
                "charts", List.of(),
                "tables", List.of()
            );
        }
    }

    /**
     * Simple summary fallback
     */
    public String generateSummary(String query) {
        Map<String, Object> result = generateAnalyticalReport(query);
        return (String) result.getOrDefault("summary", "No summary generated.");
    }

    /**
     * 🔹 Call LLM via OpenRouter Chat Completions
     */
    private String sendLLMRequest(String systemPrompt, String userInput) {
        try {
            String url = baseUrl + "/chat/completions";

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userInput)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 4500  // Balanced: detailed but not too slow
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://openrouter.ai");
            headers.set("X-Title", "Astrogenesis AI Analytical Report");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("❌ LLM API Error: " + response.getStatusCode());
                return null;
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map<String, Object> choice = choices.get(0);
            if (choice.containsKey("message"))
                return (String) ((Map<String, Object>) choice.get("message")).get("content");
            else if (choice.containsKey("text"))
                return (String) choice.get("text");

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 🎯 Get audience-specific instructions for AI analysis
     */
    private String getAudienceSpecificInstructions(String audience) {
        return switch (audience.toLowerCase()) {
            case "scientist" -> """
                    **Scientists generating new hypotheses**
                    Focus: Highlight experimental methodologies, data quality, reproducibility, and novel findings.
                    Emphasize: Unexplored variables, contradictory results, mechanistic insights, and future research directions.
                    Language: Technical, precise scientific terminology.
                    """;

            case "manager" -> """
                    **Research Managers identifying investment opportunities**
                    Focus: High-impact areas, research gaps with strategic importance, emerging trends, and resource allocation priorities.
                    Emphasize: ROI of research areas, alignment with mission objectives, interdisciplinary opportunities, and funding priorities.
                    Language: Strategic, outcome-oriented, emphasizing impact and value.
                    """;

            case "mission_architect", "architect" -> """
                    **Mission Architects planning Moon/Mars exploration**
                    Focus: Operational constraints, crew health/safety risks, life support requirements, and mission design implications.
                    Emphasize: Proven countermeasures, engineering requirements, risk mitigation, and practical applications.
                    Language: Application-focused, risk-aware, engineering-oriented.
                    """;

            default -> """
                    **General Audience** (Scientists, Managers, and Mission Architects)
                    Provide balanced coverage of scientific discoveries, strategic insights, and practical applications.
                    """;
        };
    }
}
