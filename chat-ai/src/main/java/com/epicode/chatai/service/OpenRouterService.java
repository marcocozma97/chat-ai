package com.epicode.chatai.service;

import com.epicode.chatai.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OpenRouterService {

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public RispostaLLM chiedi(String testoUtente) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        OpenRouterMessage messaggio = new OpenRouterMessage("user", testoUtente);
        OpenRouterRequest body = new OpenRouterRequest(model, List.of(messaggio));

        HttpEntity<OpenRouterRequest> richiesta = new HttpEntity<>(body, headers);

        OpenRouterResponse risposta =
                restTemplate.postForObject(apiUrl, richiesta, OpenRouterResponse.class);

        String testoRisposta = risposta.getChoices().get(0).getMessage().getContent();

        int tokenInput = 0;
        int tokenOutput = 0;
        if (risposta.getUsage() != null) {
            tokenInput = risposta.getUsage().getPromptTokens();
            tokenOutput = risposta.getUsage().getCompletionTokens();
        }

        return new RispostaLLM(testoRisposta, tokenInput + tokenOutput);
    }
}