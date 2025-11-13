package com.desarrollo.raffy.presenter;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.http.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.model.Message;
import com.desarrollo.raffy.model.PaymentsMP;
import com.desarrollo.raffy.model.Payment;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.dto.PaymentMpDTO;
import com.desarrollo.raffy.business.services.MercadoPagoService;
import com.desarrollo.raffy.business.services.PaymentService;
import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/mp")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadopagoService;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private EventsService eventService;

    private static final ObjectMapper mapper = new ObjectMapper();
    private String emailPayerMP = "jhon@doe.com";

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentMpDTO payment){
        log.warn("[PayMP] => Datos MP del pago recibido: " + payment.getPaymentMp().toString());
        try {
            // agregamos el mail del comprador de prueba para poder realizar pago de manera exitosa
            payment.getPaymentMp().getPayer().setEmail(emailPayerMP);

            Map<String, Object> payload = mapper.convertValue(payment.getPaymentMp(), Map.class);
            Map<String, Object> responseMp = mercadopagoService.createPayment(payload);
            log.warn("[PayMP] => Datos de la respuesta de crear pago: " + responseMp.toString());

            // Datos auxiliares para persistir el pago
            Map<String, Object> paymentMethod = (Map<String, Object>) responseMp.get("payment_method");
            String paymentMethodType = paymentMethod != null ? (String) paymentMethod.get("type") : null;
            String mailUser = payment.getPaymentData().getMailUser();
            Long idEvent = Long.parseLong(payment.getPaymentData().getIdEvent());

            // buscamos al usuario que intenta realizar el pago; puede ser visitante
            User payer = (User) userService.findByEmail(mailUser);
            Long payerId = (payer != null) ? payer.getId() : null;

            // buscamos al organizador del evento
            Events event = (Events) eventService.getById(idEvent);
            User creator = event.getCreator();

            Payment paymentCreated = paymentService.createPayment(
                String.valueOf(responseMp.get("id")),
                String.valueOf(responseMp.get("id")),
                payerId,
                idEvent,
                creator.getId(), // id del organizador
                payment.getPaymentMp().getTransaction_amount(),
                (String) responseMp.get("currency_id"),
                "MP",
                paymentMethodType
            );

            if (paymentCreated != null) {
                return Response.ok(responseMp, "Resultado del proceso de pago");
            } else {
                return Response.error(null, "Error al crear el pago");
            }
        } catch (RestClientResponseException e) {
            log.error("[PayMP] Error al crear pago en MP: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return Response.response(HttpStatus.valueOf(e.getRawStatusCode()), "Error en la solicitud a MercadoPago", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[PayMP] Error inesperado en processPayment: {}", e.getMessage(), e);
            return Response.response(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", null);
        }
    }

}