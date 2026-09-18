package com.epicode.chatai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RispostaLLM {
    private String testo;
    private int tokenTotali;
}