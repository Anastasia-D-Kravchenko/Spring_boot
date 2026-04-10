package com.eventflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_conversations")
@Getter @Setter
@NoArgsConstructor
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @Column(nullable = false)
    private String actionTaken; // CREATE, LIST, UPDATE, DELETE, INFO, etc.

    private boolean success = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public AiConversation(User user, String userMessage, String aiResponse, String actionTaken) {
        this.user = user;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.actionTaken = actionTaken;
    }
}
