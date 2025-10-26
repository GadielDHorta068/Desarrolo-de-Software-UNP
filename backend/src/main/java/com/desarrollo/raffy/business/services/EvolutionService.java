package com.desarrollo.raffy.business.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EvolutionService {

    private final RestTemplate restTemplate;

    @Value("${EVOLUTION_API_URL:http://evolution-api:8080}")
    private String baseUrl;

    @Value("${EVOLUTION_API_KEY:raffify2025}")
    private String apiKey;

    public EvolutionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);
        return headers;
    }

    public Map<String, Object> createInstance(Map<String, Object> payload) {
        String url = baseUrl + "/instance/create";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, defaultHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> connectInstance(String instance, String number) throws RestClientException {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(baseUrl + "/instance/connect/" + instance);
        if (number != null && !number.isBlank()) {
            builder.queryParam("number", number);
        }
        HttpEntity<Void> entity = new HttpEntity<>(defaultHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> sendText(String instance, Map<String, Object> payload) {
        String url = baseUrl + "/message/sendText/" + instance;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, defaultHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }
}