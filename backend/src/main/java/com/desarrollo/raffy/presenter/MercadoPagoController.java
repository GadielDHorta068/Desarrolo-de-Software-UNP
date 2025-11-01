package com.desarrollo.raffy.presenter;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.ResponseEntity;
// import org.springframework.http.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.model.Message;
import com.desarrollo.raffy.model.PaymentsMP;
import com.desarrollo.raffy.business.services.MercadoPagoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mp")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadopagoService;
    private static final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentsMP paymentMP){
        log.warn("Datos del pago recibido: " + paymentMP.toString());
        Map<String, Object> payload = mapper.convertValue(paymentMP, Map.class);
        Map<String, Object> response = mercadopagoService.createPayment(payload);
        log.warn("Datos de la respuesta de crear pago: " + response.toString());

        // if (payment.status === 'approved') {
        // return res.status(200).json({ success: true, data: payment });
        // } else {
        // return res.status(400).json({ success: false, data: payment });
        // }

        // de test, a definir
        return Response.ok(response, "Resultado del proceso de pago");
    }

}