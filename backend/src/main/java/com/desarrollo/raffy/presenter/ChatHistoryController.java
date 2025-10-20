package com.desarrollo.raffy.presenter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.repository.MessageRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.dto.UnreadChatSummaryDTO;
import com.desarrollo.raffy.model.Message;
import com.desarrollo.raffy.model.RegisteredUser;
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

    @PutMapping("/mark-read/{peerId}")
    @Transactional
    public ResponseEntity<?> markConversationAsRead(@PathVariable("peerId") Long peerId, Authentication authentication) {
        String currentEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        int updated = messageRepository.markAsRead(currentUser.getId(), peerId);
        return ResponseEntity.ok().body(updated);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String currentEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        long count = messageRepository.countUnread(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/unread-peers")
    public ResponseEntity<List<UnreadChatSummaryDTO>> getUnreadPeers(Authentication authentication) {
        String currentEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Object[]> rows = messageRepository.findUnreadPeersSummary(currentUser.getId());
        List<UnreadChatSummaryDTO> result = rows.stream().map(row -> {
            Long peerId = (Long) row[0];
            Long unreadCount = (Long) row[1];
            java.time.LocalDateTime lastMessageAt = (java.time.LocalDateTime) row[2];

            Optional<User> peerOpt = userRepository.findById(peerId);
            String displayName = peerOpt.map(u -> {
                if (u instanceof RegisteredUser ru) {
                    String nickname = ru.getNickname();
                    if (nickname != null && !nickname.isBlank()) return nickname;
                }
                return (u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "");
            }).orElse("Usuario " + peerId);

            return new UnreadChatSummaryDTO(peerId, displayName.trim(), unreadCount != null ? unreadCount : 0L, lastMessageAt);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}