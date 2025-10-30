package com.desarrollo.raffy.presenter;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.desarrollo.raffy.model.Payment;
import com.desarrollo.raffy.business.services.PaymentService;
import com.desarrollo.raffy.Response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para manejar operaciones relacionadas con pagos.
 * Proporciona endpoints para CRUD y operaciones de negocio de pagos.
 */
@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Obtiene todos los pagos.
     * 
     * @return ResponseEntity con la lista de todos los pagos
     */
    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        try {
            log.info("Obteniendo todos los pagos");
            List<Payment> payments = paymentService.findAll();
            return Response.ok(payments, "Pagos obtenidos exitosamente");
        } catch (Exception e) {
            log.error("Error al obtener todos los pagos: {}", e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene un pago por su ID.
     * 
     * @param id ID del pago
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(
            @PathVariable @NotNull @Positive Long id) {
        try {
            log.info("Obteniendo pago con ID: {}", id);
            Optional<Payment> payment = paymentService.findById(id);
            
            if (payment.isPresent()) {
                return Response.ok(payment.get(), "Pago encontrado");
            } else {
                return Response.notFound("Pago no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error al obtener pago con ID {}: {}", id, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo pago.
     * 
     * @param payment Datos del pago a crear
     * @return ResponseEntity con el pago creado
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody Payment payment) {
        try {
            log.info("Creando nuevo pago para usuario: {} y evento: {}", 
                    payment.getUser() != null ? payment.getUser().getId() : "null", 
                    payment.getEvent() != null ? payment.getEvent().getId() : "null");
            
            Payment createdPayment = paymentService.createPayment(payment);
            return Response.ok(createdPayment, "Pago creado exitosamente");
        } catch (Exception e) {
            log.error("Error al crear pago: {}", e.getMessage());
            return Response.error(null, "Error al crear el pago: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo pago usando parámetros directos.
     * 
     * @param paymentId ID único del pago
     * @param externalReference Referencia externa
     * @param userId ID del usuario que paga
     * @param eventId ID del evento
     * @param receiverId ID del receptor del pago
     * @param amount Monto del pago
     * @param currency Moneda del pago
     * @param paymentMethodId ID del método de pago
     * @param paymentTypeId ID del tipo de pago
     * @return ResponseEntity con el pago creado
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentWithParams(
            @RequestParam @NotBlank String paymentId,
            @RequestParam @NotBlank String externalReference,
            @RequestParam @NotNull @Positive Long userId,
            @RequestParam @NotNull @Positive Long eventId,
            @RequestParam @NotNull @Positive Long receiverId,
            @RequestParam @NotNull @Positive Double amount,
            @RequestParam @NotBlank String currency,
            @RequestParam @NotBlank String paymentMethodId,
            @RequestParam @NotBlank String paymentTypeId) {
        try {
            log.info("Creando nuevo pago con parámetros para usuario: {} y evento: {}", userId, eventId);
            
            Payment createdPayment = paymentService.createPayment(
                paymentId, externalReference, userId, eventId, receiverId, 
                amount, currency, paymentMethodId, paymentTypeId);
            return Response.ok(createdPayment, "Pago creado exitosamente");
        } catch (Exception e) {
            log.error("Error al crear pago con parámetros: {}", e.getMessage());
            return Response.error(null, "Error al crear el pago: " + e.getMessage());
        }
    }

    /**
     * Actualiza un pago existente.
     * 
     * @param id ID del pago a actualizar
     * @param payment Datos actualizados del pago
     * @return ResponseEntity con el pago actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePayment(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody Payment payment) {
        try {
            log.info("Actualizando pago con ID: {}", id);
            
            // Verificar que el pago existe
            if (!paymentService.existsById(id)) {
                return Response.notFound("Pago no encontrado con ID: " + id);
            }
            
            payment.setId(id);
            Payment updatedPayment = paymentService.save(payment);
            return Response.ok(updatedPayment, "Pago actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar pago con ID {}: {}", id, e.getMessage());
            return Response.error(null, "Error al actualizar el pago: " + e.getMessage());
        }
    }

    /**
     * Elimina un pago por su ID.
     * 
     * @param id ID del pago a eliminar
     * @return ResponseEntity confirmando la eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable @NotNull @Positive Long id) {
        try {
            log.info("Eliminando pago con ID: {}", id);
            paymentService.deleteById(id);
            return Response.ok(null, "Pago eliminado exitosamente");
        } catch (Exception e) {
            log.error("Error al eliminar pago con ID {}: {}", id, e.getMessage());
            return Response.error(null, "Error al eliminar el pago: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS DE CONSULTA PERSONALIZADOS ====================

    /**
     * Obtiene un pago por evento y receptor.
     * 
     * @param eventId ID del evento
     * @param receiverId ID del receptor
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/event/{eventId}/receiver/{receiverId}")
    public ResponseEntity<?> getPaymentByEventAndReceiver(
            @PathVariable @NotNull @Positive Long eventId,
            @PathVariable @NotNull @Positive Long receiverId) {
        try {
            log.info("Obteniendo pago para evento: {} y receptor: {}", eventId, receiverId);
            Payment payment = paymentService.findByEventIdAndReceiverId(eventId, receiverId);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago para el evento y receptor especificados");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por evento {} y receptor {}: {}", 
                    eventId, receiverId, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene un pago por usuario y evento.
     * 
     * @param userId ID del usuario
     * @param eventId ID del evento
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/user/{userId}/event/{eventId}")
    public ResponseEntity<?> getPaymentByUserAndEvent(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Obteniendo pago para usuario: {} y evento: {}", userId, eventId);
            Payment payment = paymentService.findByUserIdAndEventId(userId, eventId);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago para el usuario y evento especificados");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por usuario {} y evento {}: {}", 
                    userId, eventId, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene un pago por evento.
     * 
     * @param eventId ID del evento
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getPaymentByEvent(@PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Obteniendo pago para evento: {}", eventId);
            Payment payment = paymentService.findByEventId(eventId);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago para el evento especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por evento {}: {}", eventId, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene un pago por receptor.
     * 
     * @param receiverId ID del receptor
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<?> getPaymentByReceiver(@PathVariable @NotNull @Positive Long receiverId) {
        try {
            log.info("Obteniendo pago para receptor: {}", receiverId);
            Payment payment = paymentService.findByReceiverId(receiverId);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago para el receptor especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por receptor {}: {}", receiverId, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene un pago por usuario.
     * 
     * @param userId ID del usuario
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentByUser(@PathVariable @NotNull @Positive Long userId) {
        try {
            log.info("Obteniendo pago para usuario: {}", userId);
            Payment payment = paymentService.findByUserId(userId);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago para el usuario especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por usuario {}: {}", userId, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene pagos por estado.
     * 
     * @param status Estado del pago
     * @return ResponseEntity con el pago encontrado
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentByStatus(@PathVariable @NotNull String status) {
        try {
            log.info("Obteniendo pago con estado: {}", status);
            Payment payment = paymentService.findByStatus(status);
            
            if (payment != null) {
                return Response.ok(payment, "Pago encontrado");
            } else {
                return Response.notFound("No se encontró pago con el estado especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago por estado {}: {}", status, e.getMessage());
            return Response.error(null, "Error al obtener el pago: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS QUE DEVUELVEN MÚLTIPLES RESULTADOS ====================

    /**
     * Obtiene todos los pagos por usuario.
     * 
     * @param userId ID del usuario
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<?> getAllPaymentsByUser(@PathVariable @NotNull @Positive Long userId) {
        try {
            log.info("Obteniendo todos los pagos para usuario: {}", userId);
            List<Payment> payments = paymentService.findAllByUserId(userId);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos para el usuario especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por usuario {}: {}", userId, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los pagos por receptor.
     * 
     * @param receiverId ID del receptor
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/receiver/{receiverId}/all")
    public ResponseEntity<?> getAllPaymentsByReceiver(@PathVariable @NotNull @Positive Long receiverId) {
        try {
            log.info("Obteniendo todos los pagos para receptor: {}", receiverId);
            List<Payment> payments = paymentService.findAllByReceiverId(receiverId);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos para el receptor especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por receptor {}: {}", receiverId, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los pagos por evento.
     * 
     * @param eventId ID del evento
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/event/{eventId}/all")
    public ResponseEntity<?> getAllPaymentsByEvent(@PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Obteniendo todos los pagos para evento: {}", eventId);
            List<Payment> payments = paymentService.findAllByEventId(eventId);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos para el evento especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por evento {}: {}", eventId, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los pagos por estado.
     * 
     * @param status Estado del pago
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/status/{status}/all")
    public ResponseEntity<?> getAllPaymentsByStatus(@PathVariable @NotNull String status) {
        try {
            log.info("Obteniendo todos los pagos con estado: {}", status);
            List<Payment> payments = paymentService.findAllByStatus(status);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos con el estado especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por estado {}: {}", status, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los pagos por usuario y evento.
     * 
     * @param userId ID del usuario
     * @param eventId ID del evento
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/user/{userId}/event/{eventId}/all")
    public ResponseEntity<?> getAllPaymentsByUserAndEvent(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Obteniendo todos los pagos para usuario: {} y evento: {}", userId, eventId);
            List<Payment> payments = paymentService.findAllByUserIdAndEventId(userId, eventId);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos para el usuario y evento especificados");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por usuario {} y evento {}: {}", 
                    userId, eventId, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los pagos por evento y receptor.
     * 
     * @param eventId ID del evento
     * @param receiverId ID del receptor
     * @return ResponseEntity con la lista de pagos encontrados
     */
    @GetMapping("/event/{eventId}/receiver/{receiverId}/all")
    public ResponseEntity<?> getAllPaymentsByEventAndReceiver(
            @PathVariable @NotNull @Positive Long eventId,
            @PathVariable @NotNull @Positive Long receiverId) {
        try {
            log.info("Obteniendo todos los pagos para evento: {} y receptor: {}", eventId, receiverId);
            List<Payment> payments = paymentService.findAllByEventIdAndReceiverId(eventId, receiverId);
            
            if (!payments.isEmpty()) {
                return Response.ok(payments, "Pagos encontrados: " + payments.size());
            } else {
                return Response.notFound("No se encontraron pagos para el evento y receptor especificados");
            }
        } catch (Exception e) {
            log.error("Error al obtener pagos por evento {} y receptor {}: {}", 
                    eventId, receiverId, e.getMessage());
            return Response.error(null, "Error al obtener los pagos: " + e.getMessage());
        }
    }

    // ==================== ENDPOINTS DE LÓGICA DE NEGOCIO ====================

    /**
     * Actualiza el estado de un pago.
     * 
     * @param paymentId ID del pago
     * @param newStatus Nuevo estado del pago
     * @param statusDetail Detalle adicional del estado (opcional)
     * @return ResponseEntity con el pago actualizado
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable @NotNull @Positive Long paymentId,
            @RequestParam @NotNull @NotBlank String newStatus,
            @RequestParam(required = false) String statusDetail) {
        try {
            log.info("Actualizando estado del pago {} a: {} con detalle: {}", paymentId, newStatus, statusDetail);
            Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, newStatus, statusDetail);
            return Response.ok(updatedPayment, "Estado del pago actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al actualizar estado del pago {}: {}", paymentId, e.getMessage());
            return Response.error(null, "Error al actualizar el estado del pago: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario ha pagado por un evento específico.
     * 
     * @param userId ID del usuario
     * @param eventId ID del evento
     * @return ResponseEntity con el resultado de la verificación
     */
    @GetMapping("/verify/user/{userId}/event/{eventId}")
    public ResponseEntity<?> hasUserPaidForEvent(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Verificando si usuario {} ha pagado por evento {}", userId, eventId);
            boolean hasPaid = paymentService.hasUserPaidForEvent(userId, eventId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("eventId", eventId);
            result.put("hasPaid", hasPaid);
            
            return Response.ok(result, "Verificación completada");
        } catch (Exception e) {
            log.error("Error al verificar pago de usuario {} para evento {}: {}", 
                    userId, eventId, e.getMessage());
            return Response.error(null, "Error al verificar el pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene el pago aprobado para un evento específico.
     * 
     * @param eventId ID del evento
     * @return ResponseEntity con el pago aprobado
     */
    @GetMapping("/approved/event/{eventId}")
    public ResponseEntity<?> getApprovedPaymentForEvent(@PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Obteniendo pago aprobado para evento: {}", eventId);
            Payment approvedPayment = paymentService.getApprovedPaymentForEvent(eventId);
            
            if (approvedPayment != null) {
                return Response.ok(approvedPayment, "Pago aprobado encontrado");
            } else {
                return Response.notFound("No se encontró pago aprobado para el evento especificado");
            }
        } catch (Exception e) {
            log.error("Error al obtener pago aprobado para evento {}: {}", eventId, e.getMessage());
            return Response.error(null, "Error al obtener el pago aprobado: " + e.getMessage());
        }
    }

    /**
     * Verifica si existe un pago aprobado para un evento específico.
     * 
     * @param eventId ID del evento
     * @return ResponseEntity con el resultado de la verificación
     */
    @GetMapping("/approved/verify/event/{eventId}")
    public ResponseEntity<?> hasApprovedPaymentForEvent(@PathVariable @NotNull @Positive Long eventId) {
        try {
            log.info("Verificando si existe pago aprobado para evento: {}", eventId);
            boolean hasApprovedPayment = paymentService.hasApprovedPaymentForEvent(eventId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("eventId", eventId);
            result.put("hasApprovedPayment", hasApprovedPayment);
            
            return Response.ok(result, "Verificación completada");
        } catch (Exception e) {
            log.error("Error al verificar pago aprobado para evento {}: {}", eventId, e.getMessage());
            return Response.error(null, "Error al verificar el pago aprobado: " + e.getMessage());
        }
    }

    /**
     * Verifica si un pago existe por su ID.
     * 
     * @param paymentId ID del pago
     * @return ResponseEntity con el resultado de la verificación
     */
    @GetMapping("/exists/{paymentId}")
    public ResponseEntity<?> paymentExists(@PathVariable @NotNull @Positive Long paymentId) {
        try {
            log.info("Verificando si existe pago con ID: {}", paymentId);
            boolean exists = paymentService.existsById(paymentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", paymentId);
            result.put("exists", exists);
            
            return Response.ok(result, "Verificación completada");
        } catch (Exception e) {
            log.error("Error al verificar existencia del pago {}: {}", paymentId, e.getMessage());
            return Response.error(null, "Error al verificar la existencia del pago: " + e.getMessage());
        }
    }
}