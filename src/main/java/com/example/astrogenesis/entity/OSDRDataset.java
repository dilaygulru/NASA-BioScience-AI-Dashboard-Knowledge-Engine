package com.example.astrogenesis.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "osdr_datasets")
public class OSDRDataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private String link;
    private String doi;

    private LocalDate experimentDate;
    private LocalDateTime fetchedAt;

    @Column(columnDefinition = "TEXT")
    private String embeddingVector; // 🔹 Embedding vektör buraya kaydediliyor

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public LocalDate getExperimentDate() { return experimentDate; }
    public void setExperimentDate(LocalDate experimentDate) { this.experimentDate = experimentDate; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }

    public String getEmbeddingVector() { return embeddingVector; }
    public void setEmbeddingVector(String embeddingVector) { this.embeddingVector = embeddingVector; }
}
