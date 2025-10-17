package com.desarrollo.raffy.dto;

import java.time.LocalDateTime;

public class UnreadChatSummaryDTO {
    private Long peerId;
    private String peerDisplayName;
    private long unreadCount;
    private LocalDateTime lastMessageAt;

    public UnreadChatSummaryDTO() {}

    public UnreadChatSummaryDTO(Long peerId, String peerDisplayName, long unreadCount, LocalDateTime lastMessageAt) {
        this.peerId = peerId;
        this.peerDisplayName = peerDisplayName;
        this.unreadCount = unreadCount;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getPeerId() { return peerId; }
    public String getPeerDisplayName() { return peerDisplayName; }
    public long getUnreadCount() { return unreadCount; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }

    public void setPeerId(Long peerId) { this.peerId = peerId; }
    public void setPeerDisplayName(String peerDisplayName) { this.peerDisplayName = peerDisplayName; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}