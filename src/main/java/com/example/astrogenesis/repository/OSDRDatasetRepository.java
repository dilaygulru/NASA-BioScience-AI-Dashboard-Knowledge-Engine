package com.example.astrogenesis.repository;

import com.example.astrogenesis.entity.OSDRDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OSDRDatasetRepository extends JpaRepository<OSDRDataset, Long> {
    List<OSDRDataset> findByName(String name);
    List<OSDRDataset> findByEmbeddingVectorIsNull(); // 🔹 eksik embedding’leri bulmak için
}
