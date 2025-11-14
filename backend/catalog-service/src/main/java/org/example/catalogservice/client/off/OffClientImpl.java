package org.example.catalogservice.client.off;

import lombok.RequiredArgsConstructor;
import org.example.catalogservice.client.model.OffProduct;
import org.example.catalogservice.client.model.OffProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OffClientImpl implements OffClient {
    private static final Logger log = LoggerFactory.getLogger(OffClientImpl.class);
    private final OffProps props;
    @Qualifier("offRestClient")
    private final RestClient restClient;


    @Override
    public Optional<OffProduct> fetchByEan(String ean) {
        OffProductResponse resp = restClient.get()
                .uri("/api/v2/product/{ean}.json", ean)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    byte[] body = res.getBody() != null ? res.getBody().readAllBytes() : new byte[0];
                    throw HttpClientErrorException.create(res.getStatusCode(), "OFF product failed",
                            res.getHeaders(), body, null);
                })
                .body(OffProductResponse.class);


        if (resp == null || resp.product() == null || resp.status() != 1) {
            return Optional.empty();
        }
        return Optional.of(resp.product());
    }

    @Override
    public OffSearchResponse search (String country, int page, int pageSize) {
        URI uri = UriComponentsBuilder
                .fromPath("/api/v2/search")
                .queryParam("countries_tags", country)
                .queryParam("sort_by", "last_modified_t")
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .queryParam("fields", "code,product_name,brands,countries_tags,quantity,image_url,last_modified_t")
                .build(true)
                .toUri();

        log.info("OFF search URI => {}{}", props.baseUrl(), uri);

        int attempts = 0;
        long backoffMs = 500;
        while (true) {
            try {
                return restClient.get()
                        .uri(uri)
                        .header(HttpHeaders.USER_AGENT, props.userAgent())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(OffSearchResponse.class);
            } catch (HttpClientErrorException.TooManyRequests |
                     org.springframework.web.client.HttpServerErrorException |
                     org.springframework.web.client.ResourceAccessException e) {
                if (++attempts >= 4) throw e;
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                backoffMs = Math.min(4000, (long)(backoffMs * (1.8 + Math.random() * 0.4)));
            }
        }



    }



}