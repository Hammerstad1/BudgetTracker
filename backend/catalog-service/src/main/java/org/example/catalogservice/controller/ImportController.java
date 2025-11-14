package org.example.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.catalogservice.entity.ImportProgress;
import org.example.catalogservice.repo.ImportProgressRepository;
import org.example.catalogservice.service.ImportService;
import org.example.catalogservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/catalog/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;
    private final ImportProgressRepository importProgressRepository;

    @GetMapping("/progress")
    public List<ImportProgress> getImportProgress() {
        return importProgressRepository.findAll();
    }

    @GetMapping("/off")
    public ResponseEntity<Map<String, Object>> importOff(
            @RequestParam(defaultValue = "no") String country,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "0") int startPage
    ){
        log.info("IMPORT /off params => country={}, limit={}, pageSize={}, startPage={}",
                country, limit, pageSize, startPage);

        if (limit <= 0 || pageSize <= 0 || startPage < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid Parameters",
                            "country", country,
                            "limit", limit,
                            "pageSize", pageSize,
                            "startPage", startPage
                    ));
        }

        try {
            Map<String, Object> summary = importService.importFromOff(country, limit, pageSize, startPage);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Import failed: country={}, limit={}, pageSize={}, startPage={}",
                country, limit, pageSize, startPage, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Import failed",
                            "reason", e.getClass().getSimpleName(),
                            "message", e.getMessage()
                    ));
        }
    }
}
