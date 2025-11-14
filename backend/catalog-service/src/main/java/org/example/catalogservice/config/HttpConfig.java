package org.example.catalogservice.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {

    @Bean
    ClientHttpRequestFactory offRequestFactory(
            @Value("${off.connect-timeout:5000}") int connectTimeout,
            @Value("${off.read-timeout:10000}") int readTimeout
    ) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean(name = "offRestClient")
    @Primary
    RestClient offRestClient(
            ClientHttpRequestFactory offRequestFactory,
            @Value("${off.base-url:https://world.openfoodfacts.org}") String baseUrl,
            @Value("${off.user-agent:budget-tracker/1.0 lucashammerstad@gmail.com}") String userAgent
    ) {

        return RestClient.builder()
                .requestFactory(offRequestFactory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();

    }
}
