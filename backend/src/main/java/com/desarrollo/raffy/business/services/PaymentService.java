package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.business.repository.PaymentRepository;
import com.desarrollo.raffy.exception.PaymentNotFoundException;
import com.desarrollo.raffy.exception.PaymentValidationException;
import com.desarrollo.raffy.model.Payment;
import com.desarrollo.raffy.model.User;
import com.desarrollo.raffy.model.Events;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final EventsService eventsService;

    public PaymentService(PaymentRepository paymentRepository, UserService userService, EventsService eventsService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.eventsService = eventsService;
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
    public Payment findByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo");
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
    public Payment findByUserId(Long userId) {
        if (userId == null) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo");
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

    // ==================== MÉTODOS QUE DEVUELVEN MÚLTIPLES RESULTADOS ====================

    /**
     * Busca todos los pagos por usuario.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByUserId(Long userId) {
        if (userId == null) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo");
        }
        return paymentRepository.findAllByUserId(userId);
    }

    /**
     * Busca todos los pagos por receptor.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByReceiverId(Long receiverId) {
        if (receiverId == null) {
            throw new PaymentValidationException("ID de receptor no puede ser nulo");
        }
        return paymentRepository.findAllByReceiverId(receiverId);
    }

    /**
     * Busca todos los pagos por evento.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByEventId(Long eventId) {
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        return paymentRepository.findAllByEventId(eventId);
    }

    /**
     * Busca todos los pagos por estado.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new PaymentValidationException("Estado no puede ser nulo o vacío");
        }
        return paymentRepository.findAllByStatus(status);
    }

    /**
     * Busca todos los pagos por usuario y evento.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null) {
            throw new PaymentValidationException("ID de usuario no puede ser nulo");
        }
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        return paymentRepository.findAllByUserIdAndEventId(userId, eventId);
    }

    /**
     * Busca todos los pagos por evento y receptor.
     */
    @Transactional(readOnly = true)
    public List<Payment> findAllByEventIdAndReceiverId(Long eventId, Long receiverId) {
        if (eventId == null) {
            throw new PaymentValidationException("ID de evento no puede ser nulo");
        }
        if (receiverId == null) {
            throw new PaymentValidationException("ID de receptor no puede ser nulo");
        }
        return paymentRepository.findAllByEventIdAndReceiverId(eventId, receiverId);
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
    public Payment createPayment(String paymentId, String externalReference, Long userId, 
                               Long eventId, Long receiverId, Double amount, String currency,
                               String paymentMethodId, String paymentTypeId) {
        // Buscar las entidades
        User user = (userId != null) ? userService.findById(userId) : null;
        // esto no deberia limitar el pago, dado que pueden pagar los usuarios visitantes
        // if (user == null) {
        //     throw new PaymentValidationException("Usuario no encontrado con ID: " + userId);
        // }
        
        Events event = eventsService.getById(eventId);
        if (event == null) {
            throw new PaymentValidationException("Evento no encontrado con ID: " + eventId);
        }
        
        User receiver = userService.findById(receiverId);
        if (receiver == null) {
            throw new PaymentValidationException("Receptor no encontrado con ID: " + receiverId);
        }
        
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setExternalReference(externalReference);
        payment.setUser(user);
        payment.setEvent(event);
        payment.setReceiver(receiver);
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
        
        if (payment.getUser() == null) {
            throw new PaymentValidationException("Usuario es requerido");
        }
        
        if (payment.getEvent() == null) {
            throw new PaymentValidationException("Evento es requerido");
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
    public boolean hasUserPaidForEvent(Long userId, Long eventId) {
        if (userId == null) {
            throw new PaymentValidationException("User ID cannot be null");
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
