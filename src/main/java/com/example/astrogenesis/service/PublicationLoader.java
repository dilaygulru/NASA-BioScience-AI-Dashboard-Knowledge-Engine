package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.Publication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicationLoader {

    private static final String BASE_API =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&id=";

    private final EmbeddingService embeddingService;

    // Constructor injection
    public PublicationLoader(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    // Esnek tarih parser (tek haneli ay/günleri destekler)
    private static final DateTimeFormatter FLEX_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("yyyy[-M[-d['T'HH[:mm[:ss]]]]]")
            .toFormatter();

    public List<Publication> loadFromCsv() throws Exception {
        List<Publication> publications = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/data/SB_publication_PMC.csv"))))) {

            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] cols = line.split(",", 2);
                if (cols.length < 2) continue;

                String title = cols[0].trim();
                String link = cols[1].trim();
                String pmcId = extractPmcId(link);
                if (pmcId == null) continue;

                Publication pub = fetchMetadataFromPmc(pmcId);
                pub.setTitle(title);
                pub.setLink(link);
                pub.setFetchedAt(LocalDateTime.now());
                pub.setSource("PubMed Central / NASA Bioscience");

                // 🔹 Embedding oluştur (summary + content birleştirerek)
                String textToEmbed = (pub.getSummary() != null ? pub.getSummary() : "") + " " +
                        (pub.getContent() != null ? pub.getContent() : "");
                pub.setEmbeddingVector(embeddingService.generateEmbedding(textToEmbed));

                publications.add(pub);
                Thread.sleep(400); // API rate limit
            }
        }
        return publications;
    }

    private String extractPmcId(String url) {
        try {
            int start = url.indexOf("PMC");
            if (start == -1) return null;
            return url.substring(start + 3).replaceAll("[^0-9]", "");
        } catch (Exception e) {
            return null;
        }
    }

    private Publication fetchMetadataFromPmc(String pmcId) {
        Publication pub = new Publication();
        try {
            String apiUrl = BASE_API + pmcId + "&retmode=xml";
            Document doc = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(25000)
                    .get();

            // 🔹 Abstract
            Element abs = doc.selectFirst("abstract, AbstractText, div.abstract, p[class*=abstract]");
            pub.setSummary(abs != null ? abs.text() : "No abstract found.");

            // 🔹 Full Text (body veya section fallbacks)
            Element body = doc.selectFirst("body, full-text, sec");
            pub.setContent(body != null ? body.text() : "No full text available.");

            // 🔹 Authors
            Elements authors = doc.select("contrib-group contrib name");
            String authorList = authors.stream()
                    .map(Element::text)
                    .filter(a -> !a.isEmpty())
                    .collect(Collectors.joining(", "));
            pub.setAuthor(authorList.isEmpty() ? "Unknown" : authorList);

            // 🔹 DOI
            Element doiEl = doc.selectFirst("article-id[pub-id-type=doi]");
            if (doiEl != null)
                pub.setDoi(doiEl.text());

            // 🔹 Keywords (comma-separated)
            Elements kwEls = doc.select("kwd, keyword");
            if (!kwEls.isEmpty()) {
                String keywords = kwEls.stream()
                        .map(Element::text)
                        .filter(k -> !k.isEmpty())
                        .distinct()
                        .collect(Collectors.joining(", "));
                pub.setKeywords(keywords);
            }

            // 🔹 Publication Date (flexible)
            Element dateEl = doc.selectFirst("pub-date");
            if (dateEl != null) {
                String year = getTextOrDefault(dateEl, "year", "2000");
                String month = getTextOrDefault(dateEl, "month", "01");
                String day = getTextOrDefault(dateEl, "day", "01");
                String raw = String.format("%s-%s-%sT00:00:00", year, month, day);

                try {
                    LocalDate parsed = LocalDate.parse(raw, FLEX_DATE);
                    pub.setPublicationDate(parsed);
                } catch (Exception ex) {
                    pub.setPublicationDate(LocalDate.now());
                    System.err.println("⚠️ Date parse failed for PMC" + pmcId + ": " + raw);
                }
            } else {
                pub.setPublicationDate(LocalDate.now());
            }

            // 🔹 Extract images from publication
            Elements figures = doc.select("fig graphic, fig img, figure img, img[src*='.jpg'], img[src*='.png'], img[src*='.jpeg']");
            List<String> imageUrls = new ArrayList<>();

            for (Element fig : figures) {
                String imgUrl = fig.attr("xlink:href"); // PMC XML uses xlink:href
                if (imgUrl.isEmpty()) {
                    imgUrl = fig.attr("src"); // HTML fallback
                }

                if (!imgUrl.isEmpty()) {
                    // Convert relative URLs to absolute
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC" + pmcId + "/bin/" + imgUrl;
                    }
                    imageUrls.add(imgUrl);
                }
            }

            // Store as comma-separated string (max 5 images to avoid overflow)
            if (!imageUrls.isEmpty()) {
                String imageUrlsStr = imageUrls.stream()
                        .limit(5)
                        .distinct()
                        .collect(Collectors.joining(","));
                pub.setImageUrls(imageUrlsStr);
                System.out.println("✅ Found " + imageUrls.size() + " images for PMC" + pmcId);
            }

        } catch (Exception e) {
            pub.setSummary("Error fetching metadata: " + e.getMessage());
            pub.setContent("");
        }
        return pub;
    }

    private String getTextOrDefault(Element parent, String tag, String def) {
        Element el = parent.selectFirst(tag);
        return el != null ? el.text() : def;
    }
}
