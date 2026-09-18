package com.epicode.chatai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat")
@Getter
@Setter
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_uno_id")
    private Utente utenteUno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utente_due_id")
    private Utente utenteDue;

    @Column(nullable = false)
    private Integer tokens = 0;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastMessageSent;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}