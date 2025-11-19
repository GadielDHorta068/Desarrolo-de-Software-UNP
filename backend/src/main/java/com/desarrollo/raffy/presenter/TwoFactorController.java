package com.desarrollo.raffy.presenter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.desarrollo.raffy.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.desarrollo.raffy.business.services.TwoFactorService;

@RestController
@RequestMapping("/2fa")
@Tag(name = "2FA", description = "Autenticación de dos factores: habilitar, verificar, rotar, deshabilitar y estado")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @PostMapping(path = "/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Habilitar 2FA", description = "Habilita la autenticación de dos factores para un usuario")
    public ResponseEntity<Object> enable(@RequestBody Map<String, Object> request) {
        try {
            Object u = request.get("username");
            if (u != null) {
                Map<String, Object> status = twoFactorService.status(String.valueOf(u));
                Object enabled = status != null ? status.get("twoFactorEnabled") : null;
                if (enabled instanceof Boolean && (Boolean) enabled) {
                    return Response.conflict(Map.of("message", "2FA ya está habilitado para el usuario"), "2FA ya habilitado");
                }
            }
            Map<String, Object> res = twoFactorService.enable(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al habilitar 2FA");
        }
    }

    @PostMapping(path = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Verificar 2FA", description = "Verifica un código TOTP enviado por el usuario")
    public ResponseEntity<Object> verify(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> res = twoFactorService.verify(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al verificar 2FA");
        }
    }

    @PostMapping(path = "/verify-recovery/{username}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Verificar código de recuperación", description = "Verifica un código de respaldo para 2FA")
    public ResponseEntity<Object> verifyRecovery(@PathVariable("username") String username, @RequestBody String code) {
        try {
            Map<String, Object> res = twoFactorService.verifyRecovery(username, code);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al verificar código de recuperación");
        }
    }

    @PostMapping(path = "/rotate/{username}")
    @Operation(summary = "Rotar secreto 2FA", description = "Genera y configura un nuevo secreto de 2FA para el usuario")
    public ResponseEntity<Object> rotate(@PathVariable("username") String username, @RequestBody(required = false) Map<String, Object> body) {
        try {
            boolean authorized = false;
            if (body != null && body.get("code") != null) {
                Map<String, Object> verifyPayload = Map.of("username", username, "code", String.valueOf(body.get("code")));
                Map<String, Object> verifyRes = twoFactorService.verify(verifyPayload);
                Object verified = verifyRes != null ? verifyRes.get("verified") : null;
                authorized = (verified instanceof Boolean) && (Boolean) verified;
            } else if (body != null && body.get("recoveryCode") != null) {
                Map<String, Object> recRes = twoFactorService.verifyRecovery(username, String.valueOf(body.get("recoveryCode")));
                Object verified = recRes != null ? recRes.get("verified") : null;
                authorized = (verified instanceof Boolean) && (Boolean) verified;
            }

            if (!authorized) {
                return Response.error("Se requiere verificación previa (TOTP o código de respaldo)", "Verificación requerida");
            }

            Map<String, Object> res = twoFactorService.rotate(username);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al rotar 2FA");
        }
    }

    @PostMapping(path = "/disable/{username}")
    @Operation(summary = "Deshabilitar 2FA", description = "Deshabilita 2FA para el usuario tras verificación de código o respaldo")
    public ResponseEntity<Object> disable(@PathVariable("username") String username, @RequestBody Map<String, Object> body) {
        try {
            if ((body == null) || (!body.containsKey("code") && !body.containsKey("recoveryCode"))) {
                return Response.error("Se requiere 'code' o 'recoveryCode'", "Código requerido");
            }
            twoFactorService.disable(username, body);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al deshabilitar 2FA");
        }
    }

    @GetMapping(path = "/status/{username}")
    @Operation(summary = "Estado 2FA", description = "Obtiene el estado actual de 2FA para el usuario")
    public ResponseEntity<Object> status(@PathVariable("username") String username) {
        try {
            Map<String, Object> res = twoFactorService.status(username);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return Response.error(e.getMessage(), "Error al obtener estado de 2FA");
        }
    }
}
