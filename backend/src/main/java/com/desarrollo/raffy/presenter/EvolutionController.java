package com.desarrollo.raffy.presenter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.business.services.EvolutionService;
import com.desarrollo.raffy.dto.evolution.CreateInstanceRequest;
import com.desarrollo.raffy.dto.evolution.SendTextRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evolution")
public class EvolutionController {

    @Autowired
    private EvolutionService evolutionService;

    @PostMapping("/instances")
    public ResponseEntity<Object> createInstance(@Valid @RequestBody CreateInstanceRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("instanceName", request.getInstanceName());
            if (request.getToken() != null && !request.getToken().isBlank()) {
                payload.put("token", request.getToken());
            }
            payload.put("qrcode", request.getQrcode());
            if (request.getNumber() != null && !request.getNumber().isBlank()) {
                payload.put("number", request.getNumber());
            }
            if (request.getIntegration() != null && !request.getIntegration().isBlank()) {
                payload.put("integration", request.getIntegration());
            }

            Map<String, Object> response = evolutionService.createInstance(payload);
            return Response.ok(response, "Instancia creada en Evolution API");
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al crear instancia");
        }
    }

    @GetMapping("/instances/{instance}/connect")
    public ResponseEntity<Object> connectInstance(
        @PathVariable("instance") String instance,
        @RequestParam(value = "number", required = false) String number
    ) {
        try {
            Map<String, Object> response = evolutionService.connectInstance(instance, number);
            return Response.ok(response, "Conexión/QR generado");
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al conectar instancia");
        }
    }

    @PostMapping("/messages/text")
    public ResponseEntity<Object> sendText(@Valid @RequestBody SendTextRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("number", request.getNumber());
            payload.put("text", request.getText());
            if (request.getDelay() != null) {
                payload.put("delay", request.getDelay());
            }
            Map<String, Object> response = evolutionService.sendText(request.getInstance(), payload);
            return Response.ok(response, "Mensaje enviado");
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al enviar mensaje");
        }
    }
}