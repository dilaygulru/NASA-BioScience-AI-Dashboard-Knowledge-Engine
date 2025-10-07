package com.example.astrogenesis.service;

import com.example.astrogenesis.entity.Publication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class PmcClient {

    public Publication fetchPublicationData(String title, String link) {
        Publication pub = new Publication();
        pub.setTitle(title);
        pub.setLink(link);

        try {
            Document doc = Jsoup.connect(link).get();

            // Title
            if (pub.getTitle() == null || pub.getTitle().isEmpty()) {
                Element titleEl = doc.selectFirst("h1, h1.article-title");
                if (titleEl != null) pub.setTitle(titleEl.text());
            }

            // Author(s)
            Element authorEl = doc.selectFirst(".fm-author, .contrib-group, meta[name=citation_author]");
            if (authorEl != null) {
                pub.setAuthor(authorEl.text());
            }

            // Abstract
            Element abs = doc.selectFirst("div.abstr, div.abstract, section#abstract, p.abstract");
            if (abs != null) {
                pub.setSummary(abs.text());
            } else {
                pub.setSummary("Abstract not found.");
            }

            // Content
            Element content = doc.selectFirst("div.tsec, div.article, div#body, div.main-content");
            if (content != null) {
                pub.setContent(content.text());
            } else {
                pub.setContent("Full text unavailable.");
            }

        } catch (Exception e) {
            pub.setSummary("Error fetching: " + e.getMessage());
            pub.setContent("");
        }

        return pub;
    }
}
