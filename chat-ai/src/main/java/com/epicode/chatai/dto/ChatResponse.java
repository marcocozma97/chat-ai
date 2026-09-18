package com.epicode.chatai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ChatResponse {
    private UUID id;
    private String altroUtente;   // username dell'altro partecipante
    private Integer tokens;
    private Instant createdAt;
    private Instant lastMessageSent;
}