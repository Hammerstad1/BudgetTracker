package org.example.catalogservice.dto;

public record ImportSummary (
        int requested,
        int imported,
        int skipped,
        int failed
){
}
