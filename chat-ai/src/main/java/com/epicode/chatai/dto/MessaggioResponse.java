package com.epicode.chatai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class MessaggioResponse {
    private UUID id;
    private String mittenteUsername;
    private String testo;
    private Instant createdAt;
}