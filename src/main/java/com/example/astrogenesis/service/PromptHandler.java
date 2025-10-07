package com.example.astrogenesis.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptHandler {

    public Map<String, Object> handlePrePrompt(String query) {
        if (query == null || query.isBlank()) {
            return Map.of("summary", "⚠️ Please enter a scientific question to continue.");
        }

        String lower = query.toLowerCase();

        // 1️⃣ Greetings
        if (lower.matches(".*\\b(hello|hi|hey|selam|merhaba|how are you|thanks|thank you)\\b.*")) {
            return Map.of("summary",
                    "👋 Hello! I’m the **Astrogenesis AI Assistant**. "
                            + "I summarize NASA bioscience research, generate analytical reports, "
                            + "and explore OSDR datasets. Try asking: "
                            + "*‘microgravity bone loss in astronauts’* or "
                            + "*‘radiation effects on muscle tissue’*.");
        }

        // 2️⃣ Off-topic queries
        if (lower.matches(".*\\b(joke|story|weather|movie|game|song)\\b.*")) {
            return Map.of("summary",
                    "😅 I'm focused on NASA bioscience and space biology research. "
                            + "\n\nLet's explore:\n"
                            + "• How microgravity affects human physiology\n"
                            + "• Radiation's impact on biological systems\n"
                            + "• Plant growth experiments in space\n"
                            + "• Mission planning for Moon and Mars exploration");
        }

        // 3️⃣ Check if it contains valid NASA bioscience keywords
        boolean hasValidTopic = lower.matches(".*\\b(microgravity|radiation|spaceflight|bioscience|osdr|cell|tissue|gene|astronaut|plant|bone|muscle|immune|moon|mars|exploration|space|gravity|experiment|study|research|effect|impact|density|loss|growth)\\b.*");

        // If it has valid keywords and is asking a real question → let it pass to RAG
        if (hasValidTopic && (lower.contains("what") || lower.contains("how") || lower.contains("why") ||
            lower.contains("effect") || lower.contains("impact") || lower.contains("?") || lower.length() > 20)) {
            return null; // null means "proceed to RAG/LLM"
        }

        // 4️⃣ Invalid / unclear topic
        if (!hasValidTopic) {
            return Map.of("summary",
                    "🤔 I couldn't find a clear NASA bioscience topic. "
                            + "\n\n**Try asking about:**\n"
                            + "• Research progress: *'What progress has been made in radiation protection?'*\n"
                            + "• Knowledge gaps: *'What are the critical gaps in microgravity bone loss research?'*\n"
                            + "• Scientific consensus: *'Where do studies agree/disagree on plant growth in space?'*\n"
                            + "• Mission planning: *'How does microgravity affect astronaut health on Mars missions?'*");
        }

        // 5️⃣ Default - vague but topic-related
        return Map.of("summary",
                "🛰️ **Welcome to Astrogenesis AI Dashboard**\n\n"
                        + "I'm specialized in analyzing 608+ NASA bioscience publications to support:\n"
                        + "• **Scientists** → Generate new hypotheses\n"
                        + "• **Managers** → Identify investment opportunities\n"
                        + "• **Mission Architects** → Plan safe Moon/Mars exploration\n\n"
                        + "**What I can do:**\n"
                        + "✓ Summarize research progress & identify gaps\n"
                        + "✓ Analyze scientific consensus & disagreements\n"
                        + "✓ Provide actionable mission planning insights\n"
                        + "✓ Generate hypotheses for future experiments\n\n"
                        + "**Try asking**: *'What are the effects of microgravity on bone density?'*");
    }
}
