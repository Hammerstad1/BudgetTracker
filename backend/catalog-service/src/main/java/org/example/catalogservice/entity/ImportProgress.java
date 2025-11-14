package org.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "import_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String countryCode;

    @Column(nullable = false)
    private int lastPage;

    @Column(nullable = false)
    private int totalImported;

    @Column(nullable = false)
    private int totalUpdated;

    private String lastRunStatus;

    private java.time.OffsetDateTime lastRunAt;
}
