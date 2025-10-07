package com.example.astrogenesis.repository;

import com.example.astrogenesis.entity.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {

    @Query("""
        SELECT p FROM Publication p
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(CAST(p.summary AS string)) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Publication> searchPublications(@Param("query") String query);
}
