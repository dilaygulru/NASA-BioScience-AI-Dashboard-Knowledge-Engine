package com.example.astrogenesis.controller;

import com.example.astrogenesis.entity.Publication;
import com.example.astrogenesis.service.PublicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing and exposing Publication data.
 * Handles both Thymeleaf page rendering and REST-style JSON endpoints
 * for the frontend (JS dynamic fetch via main.js).
 */
@Controller
@RequestMapping("/publications")
public class PublicationController {

    private final PublicationService service;

    public PublicationController(PublicationService service) {
        this.service = service;
    }

    /**
     * 🟢 Render the research publications page.
     * Accessible via: http://localhost:8080/publications
     */
    @GetMapping
    public String listPublications(Model model) {
        model.addAttribute("publications", service.getAllPublications());
        return "research"; // ✅ templates/research.html
    }

    /**
     * 🔹 JSON endpoint — returns all publications for frontend dynamic table.
     * Used by: main.js → loadPublications()
     * Example: GET /publications/api
     */
    @GetMapping("/api")
    @ResponseBody
    public List<Publication> getAllPublications() {
        return service.getAllPublications();
    }

    /**
     * 🔍 JSON search endpoint — filters publications by query (title, keywords, etc.)
     * Used by: main.js → searchForm
     * Example: GET /publications/search?q=plant
     */
    @GetMapping("/search")
    @ResponseBody
    public List<Publication> search(@RequestParam(value = "q", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return service.getAllPublications(); // fallback: return all
        }
        return service.searchPublications(query.trim());
    }
}
