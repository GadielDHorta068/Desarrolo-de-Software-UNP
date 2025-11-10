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
        // agregamos el mail del comprador de prueba para poder realizar pago de manera exitosa
        payment.getPaymentMp().getPayer().setEmail(emailPayerMP);
        Map<String, Object> payload = mapper.convertValue(payment.getPaymentMp(), Map.class);
        Map<String, Object> responseMp = mercadopagoService.createPayment(payload);
        log.warn("[PayMP] => Datos de la respuesta de crear pago: " + responseMp.toString());

        // creamos el dato del pago y lo persistimos, ej de uso de API
        // Payment paymentCreated = paymentService.createPayment(
        //     "1",
        //     "VER_QUE_VA",
        //     1L,
        //     1L,
        //     1L,
        //     2000.00,
        //     "ARS",
        //     "debvisa",
        //     "tipo_pago"
        // );

        Map<String, Object> paymentMethod = (Map<String, Object>) responseMp.get("payment_method");
        String idUser = payment.getPaymentData().getIdUser();
        String mailUser = payment.getPaymentData().getMailUser();
        // log.warn("[PayMP] => IdUser: " + idUser);
        Long idEvent = Long.parseLong(payment.getPaymentData().getIdEvent());
        // buscamos al usuario que intenta realizar el pago
        User payer = (User) userService.findByEmail(mailUser);
        // buscamos al organizador del evento
        Events event = (Events) eventService.getById(idEvent);
        User creator = event.getCreator();

        Payment paymentCreated;
        paymentCreated = paymentService.createPayment(
            String.valueOf(responseMp.get("id")),
            String.valueOf(responseMp.get("id")),
            payer.getId(),
            idEvent,
            creator.getId(),         // aca deberia ir el id del organizador (consultar como recuperarlo)
            payment.getPaymentMp().getTransaction_amount(),
            (String) responseMp.get("currency_id"),
            "MP",
            (String) paymentMethod.get("type")
        );


        // if (payment.status === 'approved') {
        // return res.status(200).json({ success: true, data: payment });
        // } else {
        // return res.status(400).json({ success: false, data: payment });
        // }

        if (paymentCreated != null) {
            // faltaria esto
            // auditLogsService.createAuditEvent(created);
            return Response.ok(responseMp, "Resultado del proceso de pago");
        } else {
            return new ResponseEntity<>("Error al crear el evento", HttpStatus.BAD_REQUEST);
        }

        // de test, a definir
        // return Response.ok(response, "Resultado del proceso de pago");
    }

}