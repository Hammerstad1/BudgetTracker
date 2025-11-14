package org.example.catalogservice.repo;

import org.example.catalogservice.entity.ImportProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportProgressRepository extends JpaRepository<ImportProgress, Long> {
    Optional<ImportProgress> findByCountryCode(String country);
}
