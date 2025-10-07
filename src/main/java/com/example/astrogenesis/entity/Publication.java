package com.example.astrogenesis.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "publications")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String author;

    @Column(length = 500)
    private String link;

    @Column(length = 200)
    private String doi;

    // 🔹 Keywords düz metin olarak tutuluyor (örnek: "microgravity, stem cells, bone loss")
    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(length = 200)
    private String source;

    // 🔹 Yayın tarihi (örneğin: 2024-03-15)
    private LocalDate publicationDate;

    // 🔹 Verinin çekildiği tarih (timestamp)
    private LocalDateTime fetchedAt;

    // 🔹 Embedding vector (ileride LLM için kullanılacak)
    @Column(columnDefinition = "TEXT")
    private String embeddingVector;

    // 🔹 Image URLs (comma-separated if multiple)
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    // 🔹 NASA OSDR veri kümeleri ile ilişki (çoktan çoğa)
    @ManyToMany
    @JoinTable(
            name = "publication_osdr",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "osdr_id")
    )
    private List<OSDRDataset> osdrDatasets;

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }

    public String getEmbeddingVector() { return embeddingVector; }
    public void setEmbeddingVector(String embeddingVector) { this.embeddingVector = embeddingVector; }

    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }

    public List<OSDRDataset> getOsdrDatasets() { return osdrDatasets; }
    public void setOsdrDatasets(List<OSDRDataset> osdrDatasets) { this.osdrDatasets = osdrDatasets; }
}
