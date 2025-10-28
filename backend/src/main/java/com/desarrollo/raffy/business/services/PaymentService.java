package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.business.repository.PaymentRepository;
import com.desarrollo.raffy.exception.PaymentNotFoundException;
import com.desarrollo.raffy.exception.PaymentValidationException;
import com.desarrollo.raffy.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // ==================== CRUD BÁSICOS ====================
    
    /**
     * Guarda un pago en la base de datos.
     */
    @Transactional
    public Payment save(Payment payment) {
        validatePayment(payment);
        return paymentRepository.save(payment);
    }

    /**
     * Busca un pago por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        if (id == null) {
            throw new PaymentValidationException("ID de pago no puede ser nulo");
        }
        return paymentRepository.findById(id);
    }

    /**
     * Busca un pago por su ID y lanza excepción si no existe.
     */
    @Transactional(readOnly = true)
    public Payment findByIdOrThrow(Long id) {
        if (id == null) {
            throw new PaymentValidationException("ID de pago no puede ser nulo");
        }
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    /**
     * Obtiene todos los pagos
     */
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /**
     * Elimina un pago por su ID.
     */
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new PaymentValidationException("ID de pago no puede ser nulo");
        }
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException(id);
        }
        paymentRepository.deleteById(id);
    }

    /**
     * Verifica si existe un pago con el ID dado.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            throw new PaymentValidationException("ID de pago no puede ser nulo");
        }
        return paymentRepository.existsById(id);
    }

    // ==================== MÉTODOS PERSONALIZADOS ====================

    /**
     * Busca pago por evento y receptor.
     */
    @Transactional(readOnly = true)
    public Payment findByEventIdAndReceiverId(Long eventId, Long receiverId) {
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        if (receiverId == null) {
            throw new PaymentValidationException("ID de receptor no puede ser nulo");
        }
        return paymentRepository.findByEventIdAndReceiverId(eventId, receiverId);
    }

    /**
     * Busca pago por usuario y evento.
     */
    @Transactional(readOnly = true)
    public Payment findByUserIdAndEventId(String userId, Long eventId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo o vacío");
        }
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        return paymentRepository.findByUserIdAndEventId(userId, eventId);
    }

    /**
     * Busca pago por evento.
     */
    @Transactional(readOnly = true)
    public Payment findByEventId(Long eventId) {
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        return paymentRepository.findByEventId(eventId);
    }

    /**
     * Busca pago por receptor.
     */
    @Transactional(readOnly = true)
    public Payment findByReceiverId(Long receiverId) {
        if (receiverId == null) {
            throw new PaymentValidationException("ID de receptor no puede ser nulo");
        }
        return paymentRepository.findByReceiverId(receiverId);
    }

    /**
     * Busca pago por usuario.
     */
    @Transactional(readOnly = true)
    public Payment findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo o vacío");
        }
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Busca pago por estado.
     */
    @Transactional(readOnly = true)
    public Payment findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new PaymentValidationException("Estado no puede ser nulo o vacío");
        }
        return paymentRepository.findByStatus(status);
    }

    // ==================== MÉTODOS DE NEGOCIO ====================

    /**
     * Actualiza el estado de un pago.
     */
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, String newStatus, String statusDetail) {
        if (paymentId == null) {
            throw new PaymentValidationException("ID de pago no puede ser nulo");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new PaymentValidationException("Nuevo estado no puede ser nulo o vacío");
        }
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        payment.setStatus(newStatus);
        payment.setStatusDetail(statusDetail);
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    /**
     * Crea un nuevo pago
     */
    public Payment createPayment(String paymentId, String externalReference, String userId, 
                               Long eventId, Long receiverId, Double amount, String currency,
                               String paymentMethodId, String paymentTypeId) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setExternalReference(externalReference);
        payment.setUserId(userId);
        payment.setEventId(eventId);
        payment.setReceiverId(receiverId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentMethodId(paymentMethodId);
        payment.setPaymentTypeId(paymentTypeId);
        payment.setStatus("pending"); // Estado inicial
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        return save(payment);
    }

    /**
     * Crea un nuevo pago con validaciones básicas.
     */
    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment == null) {
            throw new PaymentValidationException("Pago no puede ser nulo");
        }
        if (payment.getId() != null) {
            throw new PaymentValidationException("Nuevo pago no debe tener ID");
        }
        
        validatePayment(payment);
        
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    /**
     * Valida los datos básicos de un pago.
     */
    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new PaymentValidationException("Pago no puede ser nulo");
        }
        
        if (payment.getUserId() == null) {
            throw new PaymentValidationException("ID de usuario es requerido");
        }
        
        if (payment.getEventId() == null) {
            throw new PaymentValidationException("ID de evento es requerido");
        }
        
        if (payment.getAmount() == null || payment.getAmount() <= 0.0) {
            throw new PaymentValidationException("Monto debe ser mayor a cero");
        }
        
        if (payment.getCurrency() == null || payment.getCurrency().trim().isEmpty()) {
            throw new PaymentValidationException("Moneda es requerida");
        }
        
        if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) {
            throw new PaymentValidationException("Estado de pago es requerido");
        }
        
        // Establecer timestamps si no están presentes
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        payment.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Verifica si un usuario ya tiene un pago para un evento específico.
     */
    @Transactional(readOnly = true)
    public boolean hasUserPaidForEvent(String userId, Long eventId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("User ID cannot be null or empty");
        }
        if (eventId == null) {
            throw new PaymentValidationException("Event ID cannot be null");
        }
        return findByUserIdAndEventId(userId, eventId) != null;
    }

    /**
     * Obtiene el pago aprobado para un evento (si existe).
     */
    @Transactional(readOnly = true)
    public Payment getApprovedPaymentForEvent(Long eventId) {
        if (eventId == null) {
            throw new PaymentValidationException("Event ID cannot be null");
        }
        Payment payment = paymentRepository.findByEventId(eventId);
        return (payment != null && "approved".equals(payment.getStatus())) ? payment : null;
    }

    /**
     * Verifica si existe un pago aprobado para un evento.
     */
    @Transactional(readOnly = true)
    public boolean hasApprovedPaymentForEvent(Long eventId) {
        if (eventId == null) {
            throw new PaymentValidationException("Event ID cannot be null");
        }
        return getApprovedPaymentForEvent(eventId) != null;
    }
}
