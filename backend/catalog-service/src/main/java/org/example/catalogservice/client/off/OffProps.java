package org.example.catalogservice.client.off;



import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "off")
public record OffProps (
        String baseUrl,
        String userAgent,
        Duration timeout
){
}
