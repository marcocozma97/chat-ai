package com.epicode.chatai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NuovaChatRequest {

    @NotBlank(message = "Devi indicare l'utente con cui chattare")
    private String usernameDestinatario;
}