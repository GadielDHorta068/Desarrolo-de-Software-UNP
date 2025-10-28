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

import com.desarrollo.raffy.dto.CreatePaymentRequest;

@Service
public class MercadoPagoService {

    private final RestTemplate restTemplate;

    // URL url = new URL("https://api.mercadopago.com/v1/payments");
    //         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //         conn.setRequestMethod("POST");
    //         conn.setRequestProperty("Authorization", "TEST-5661274554438572-102416-924ed3e9f367098b2d776c8933db61ac-501812522");

    private String baseUrl = "https://api.mercadopago.com";
    private String tokenAccess = "TEST-5661274554438572-102416-924ed3e9f367098b2d776c8933db61ac-501812522";

    public MercadoPagoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.set("Authorization", tokenAccess);
        headers.set("Authorization", "TEST-5661274554438572-102416-924ed3e9f367098b2d776c8933db61ac-501812522");
        return headers;
    }

    // creamos el pago con el api de MP
    public Map<String, Object> createPayment(Map<String, Object> payloadPayment) {
    // public Map<String, Object> createPayment(CreatePaymentRequest payloadPayment) {
        // String url = baseUrl + "/v1/payments";
        String url = "https://api.mercadopago.com/v1/payments";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payloadPayment, defaultHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        System.out.println("paymentResponse: " + response);
        return response.getBody();
    }

}