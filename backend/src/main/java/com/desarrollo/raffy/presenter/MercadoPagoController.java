package com.desarrollo.raffy.presenter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

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
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RaffleNumber;
import com.desarrollo.raffy.model.auditlog.AuditActionType;
import com.desarrollo.raffy.dto.PaymentMpDTO;
import com.desarrollo.raffy.business.services.MercadoPagoService;
import com.desarrollo.raffy.business.services.PaymentService;
import com.desarrollo.raffy.business.services.EventsService;
import com.desarrollo.raffy.business.services.UserService;
import com.desarrollo.raffy.business.services.EmailService;
import com.desarrollo.raffy.business.services.RaffleNumberService;
import com.desarrollo.raffy.business.services.AuditLogsService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/mp")
@Tag(name = "MercadoPago", description = "Integración de pagos con MercadoPago")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadopagoService;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private EventsService eventService;
    @Autowired
    private RaffleNumberService raffleNumberService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuditLogsService auditLogsService;

    private static final ObjectMapper mapper = new ObjectMapper();
    private String emailPayerMP = "jhon@doe.com";

    @SuppressWarnings("deprecation")
    @PostMapping("/process-payment")
    @Operation(summary = "Procesar pago", description = "Procesa un pago con datos de MercadoPago y persiste el resultado")
    public ResponseEntity<?> processPayment(@RequestBody PaymentMpDTO payment){
        // log.warn("[PayMP] => Datos MP del pago recibido: " + payment.getPaymentMp().toString());
        try {
            // agregamos el mail del comprador de prueba para poder realizar pago de manera exitosa
            payment.getPaymentMp().getPayer().setEmail(emailPayerMP);

            Map<String, Object> payload = mapper.convertValue(payment.getPaymentMp(), Map.class);
            Map<String, Object> responseMp = mercadopagoService.createPayment(payload);
            // log.warn("[PayMP] => Datos de la respuesta de crear pago: " + responseMp.toString());

            // Datos auxiliares para persistir el pago
            Map<String, Object> paymentMethod = (Map<String, Object>) responseMp.get("payment_method");
            String paymentMethodType = paymentMethod != null ? (String) paymentMethod.get("type") : null;
            String mailUser = payment.getPaymentData().getMailUser();
            Long idEvent = Long.parseLong(payment.getPaymentData().getIdEvent());

            // buscamos al usuario que intenta realizar el pago; puede ser visitante
            User buyer = (User) userService.findByEmail(mailUser);
            Long buyerId = (buyer != null) ? buyer.getId() : null;

            // buscamos al organizador del evento
            Raffle event = (Raffle) eventService.getById(idEvent);
            User creator = event.getCreator();

            String paymentStatus = (String) responseMp.get("status");
            Payment paymentCreated = paymentService.createPayment(
                String.valueOf(responseMp.get("id")),
                String.valueOf(responseMp.get("id")),
                buyerId,
                idEvent,
                creator.getId(), // id del organizador
                payment.getPaymentMp().getTransaction_amount(),
                (String) responseMp.get("currency_id"),
                "MP",
                paymentMethodType,
                paymentStatus
            );

            // asigno el pago a cada nro comprado
            int[] buyedNumbers = payment.getPaymentData().getNumbers();
            for (int number : buyedNumbers) {
                RaffleNumber rafNumber = raffleNumberService.findRaffleNumberByEventIdAndNumber(number, idEvent);
                if(rafNumber != null){
                    rafNumber.setPayment(paymentCreated);
                    raffleNumberService.save(rafNumber);
                }
            }

            List<Integer> purchasedNumbers = Arrays.stream(buyedNumbers)   // convierte el int[] en IntStream
                        .boxed()           // convierte cada int en Integer
                        .collect(Collectors.toList());
                        
            // si se logro crear el pago con exito y su estado es aprobado
            if((paymentCreated != null) && "approved".equals(paymentCreated.getStatus())) {

                // Construimos datos comunes (correo y WhatsApp)
                String buyerName = (buyer.getName() != null ? buyer.getName() : "") +
                                (buyer.getSurname() != null ? (" " + buyer.getSurname()) : "");
                
                // creamos la auditoria
                auditLogsService.logAction(
                    event.getId(), 
                    buyer.getName() + " " + buyer.getSurname(), 
                    AuditActionType.NUMBER_PURCHASED, 
                    String.format("Números comprados: %s", purchasedNumbers.toString()));

                // Enviar correo de confirmación
                try {
                    // log.warn("[PayMP] => Email a donde se envia la info: " + buyer.getEmail());
                    emailService.sendRaffleNumbersPurchasedEmail(
                        buyer.getEmail(),
                        buyerName.trim().isEmpty() ? (buyer.getEmail() != null ? buyer.getEmail() : "Usuario") : buyerName.trim(),
                        event.getId(),
                        event.getTitle(),
                        event.getPriceOfNumber(),
                        purchasedNumbers
                    );
                } catch (Exception e) {
                    System.err.println("⚠️ Error enviando correo de confirmación de números de rifa: " + e.getMessage());
                    e.printStackTrace();
                }
                // Enviar WhatsApp de confirmación de compra
                String buyerPhone = buyer.getCellphone();
                // log.warn("[PayMP] => Celular a donde se envia la info: " + buyerPhone);
                if (buyerPhone != null && !buyerPhone.isEmpty()) {
                    try {
                        String numbersText = purchasedNumbers.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                        String displayName = buyerName.trim().isEmpty() ? (buyer.getEmail() != null ? buyer.getEmail() : "Usuario") : buyerName.trim();
                        String msg = "Hola " + displayName + ",\n"
                                + "*Confirmación de compra de números*\n"
                                + "Rifa: _" + event.getTitle() + "_\n"
                                + "Precio por número: *$" + String.format("%.2f", event.getPriceOfNumber()) + "*\n"
                                + "Números adquiridos: " + numbersText + "\n"
                                + "_¡Gracias por participar!_";
                        raffleNumberService.sendWhatsAppText(buyer.getCellphone(), msg);
                    } catch (Exception e) {
                        System.err.println("⚠️ Error enviando WhatsApp de confirmación de compra: " + e.getMessage());
                    }
                }
            }
            else{
                String actionLog = "Pago en espera: %s";
                if(!"pending".equals(paymentCreated.getStatus())){
                    actionLog = "Pago fallido: %s";
                }
                // creamos la auditoria
                auditLogsService.logAction(
                    event.getId(), 
                    buyer.getName() + " " + buyer.getSurname(), 
                    "pending".equals(paymentCreated.getStatus()) ? AuditActionType.NUMBER_PURCHASED: AuditActionType.NUMBER_PURCHASED_FAILED, 
                    String.format(actionLog, purchasedNumbers.toString()));
            }

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