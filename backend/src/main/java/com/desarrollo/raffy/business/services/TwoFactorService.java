package com.desarrollo.raffy.business.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TwoFactorService {

    private final RestTemplate restTemplate;

    @Value("${TWOFA_API_URL:http://twofa:8080}")
    private String baseUrl;

    public TwoFactorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String, Object> enable(Map<String, Object> payload) {
        String url = baseUrl + "/api/2fa/enable";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, jsonHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> verify(Map<String, Object> payload) {
        String url = baseUrl + "/api/2fa/verify";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, jsonHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> verifyRecovery(String username, String recoveryCode) {
        String url = baseUrl + "/api/2fa/verify-recovery/" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<>(recoveryCode, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> rotate(String username) {
        String url = baseUrl + "/api/2fa/rotate/" + username;
        HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public void disable(String username, Map<String, Object> payload) {
        String url = baseUrl + "/api/2fa/disable/" + username;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, jsonHeaders());
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public Map<String, Object> status(String username) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/2fa/status/" + username);
        HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }
}
