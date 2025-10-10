package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.repository.MessageRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.model.Message;
import com.desarrollo.raffy.model.User;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/history/{destinatarioId}")
    public ResponseEntity<List<Message>> getHistory(@PathVariable("destinatarioId") Long destinatarioId,
                                                    Authentication authentication) {
        String currentEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Message> history = messageRepository.findConversation(currentUser.getId(), destinatarioId);
        return ResponseEntity.ok(history);
    }
}