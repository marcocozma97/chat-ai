package com.epicode.chatai.controller;

import com.epicode.chatai.dto.MessaggioResponse;
import com.epicode.chatai.dto.NuovoMessaggioRequest;
import com.epicode.chatai.dto.SuggerimentoRequest;
import com.epicode.chatai.dto.SuggerimentoResponse;
import com.epicode.chatai.service.MessaggioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/message")
public class MessaggioController {

    private final MessaggioService messaggioService;

    public MessaggioController(MessaggioService messaggioService) {
        this.messaggioService = messaggioService;
    }

    @PostMapping
    public MessaggioResponse nuovoMessaggio(@Valid @RequestBody NuovoMessaggioRequest request,
                                            Principal principal) {
        return messaggioService.nuovoMessaggio(principal.getName(), request.getIdChat(), request.getTesto());
    }

    @GetMapping("/{chatId}")
    public Page<MessaggioResponse> getMessaggi(@PathVariable UUID chatId,
                                               @RequestParam(defaultValue = "0") int pagina) {
        return messaggioService.getMessaggiByChat(chatId, pagina);
    }

    @PostMapping("/suggerisci")
    public SuggerimentoResponse suggerisci(@Valid @RequestBody SuggerimentoRequest request) {
        String proposta = messaggioService.proponiRisposta(request.getIdChat(), request.getMessaggioRicevuto());
        return new SuggerimentoResponse(proposta);
    }
}