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

    @SuppressWarnings("null")
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message payload, Authentication authentication) {
        if (authentication == null || payload == null || payload.getDestinatarioId() == null) {
            return;
        }
        String currentEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentEmail);
        if (currentUserOpt.isEmpty()) {
            return;
        }

        User currentUser = currentUserOpt.get();
        if (currentUser.getId().equals(payload.getDestinatarioId())) {
            return;
        }
        Optional<User> recipientOpt = userRepository.findById(payload.getDestinatarioId());
        if (recipientOpt.isEmpty()) {
            return;
        }
        payload.setRemitenteId(currentUser.getId());
        payload.setFechaEnvio(LocalDateTime.now());
        payload.setLeido(false);

        Message saved = messageRepository.save(payload);

        String recipientEmail = recipientOpt.get().getEmail();
        messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/private-messages", saved);
    }
}
