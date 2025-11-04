package com.desarrollo.raffy.business.services;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

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

import com.desarrollo.raffy.dto.CreatePaymentRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MercadoPagoService {

    private final RestTemplate restTemplate;

    // private String baseUrl = "https://api.mercadopago.com";
    // private String tokenAccess = "TEST-5661274554438572-102416-924ed3e9f367098b2d776c8933db61ac-501812522";

    @Value("${mercadopago.url}")
    private String baseUrl;
    @Value("${mercadopago.acces_token}")
    private String tokenAccess;

    public MercadoPagoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());
        headers.setBearerAuth(this.tokenAccess); 
        return headers;
    }

    // creamos el pago con el api de MP
    public Map<String, Object> createPayment(Map<String, Object> payloadPayment) {
        // log.warn("[mpService] => Datos del pago recibido: " + payloadPayment.toString());
        String url = baseUrl + "/v1/payments";
        // log.warn("[mpService] => paymentResponse - url MP: " + url);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payloadPayment, defaultHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        // log.warn("paymentResponse: " + response.toString());
        return response.getBody();
    }

}