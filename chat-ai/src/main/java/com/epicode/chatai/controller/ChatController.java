package com.epicode.chatai.controller;

import com.epicode.chatai.dto.ChatResponse;
import com.epicode.chatai.dto.NuovaChatRequest;
import com.epicode.chatai.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse creaChat(@Valid @RequestBody NuovaChatRequest request, Principal principal) {
        return chatService.creaChat(principal.getName(), request.getUsernameDestinatario());
    }

    @GetMapping
    public List<ChatResponse> getChat(Principal principal) {
        return chatService.getChatDiUtente(principal.getName());
    }

    @PatchMapping("/{id}")
    public void cancellaChat(@PathVariable UUID id) {
        chatService.cancellaChat(id);
    }
}