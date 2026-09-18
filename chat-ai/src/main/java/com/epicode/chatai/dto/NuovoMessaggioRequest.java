package com.epicode.chatai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NuovoMessaggioRequest {

    @NotNull(message = "idChat è obbligatorio")
    private UUID idChat;

    @NotBlank(message = "Il testo del messaggio è obbligatorio")
    private String testo;
}