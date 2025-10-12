package com.desarrollo.raffy.presenter;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.desarrollo.raffy.business.repository.MessageRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.model.Message;
import com.desarrollo.raffy.model.User;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message payload, Authentication authentication) {
        // Identificar remitente desde el contexto de seguridad
        String currentEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentEmail);
        if (currentUserOpt.isEmpty()) {
            return; // No se pudo resolver el usuario autenticado
        }

        User currentUser = currentUserOpt.get();
        payload.setRemitenteId(currentUser.getId());
        payload.setFechaEnvio(LocalDateTime.now());
        payload.setLeido(false);

        // Persistir mensaje
        Message saved = messageRepository.save(payload);

        // Obtener email/username del destinatario para enrutamiento de usuario
        Optional<User> recipientOpt = userRepository.findById(saved.getDestinatarioId());
        if (recipientOpt.isPresent()) {
            String recipientEmail = recipientOpt.get().getEmail();
            messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/private-messages", saved);
        }
    }
}