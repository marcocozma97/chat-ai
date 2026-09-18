package com.epicode.chatai.service;

import com.epicode.chatai.dto.ChatResponse;
import com.epicode.chatai.entity.Chat;
import com.epicode.chatai.entity.Utente;
import com.epicode.chatai.repository.ChatRepository;
import com.epicode.chatai.repository.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UtenteRepository utenteRepository;

    public ChatService(ChatRepository chatRepository, UtenteRepository utenteRepository) {
        this.chatRepository = chatRepository;
        this.utenteRepository = utenteRepository;
    }

    // Crea una chat tra l'utente loggato e un altro utente
    public ChatResponse creaChat(String usernameCorrente, String usernameDestinatario) {
        if (usernameCorrente.equals(usernameDestinatario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Non puoi aprire una chat con te stesso");
        }

        Utente io = utenteRepository.findByUsername(usernameCorrente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        Utente altro = utenteRepository.findByUsername(usernameDestinatario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utente non trovato: " + usernameDestinatario));

        // se una chat tra i due esiste già, la restituisco invece di duplicarla
        Optional<Chat> esistente = chatRepository.findChatTraDueUtenti(io.getId(), altro.getId());
        if (esistente.isPresent()) {
            return toResponse(esistente.get(), io.getId());
        }

        Instant adesso = Instant.now();
        Chat chat = new Chat();
        chat.setUtenteUno(io);
        chat.setUtenteDue(altro);
        chat.setTokens(0);
        chat.setCreatedAt(adesso);
        chat.setLastMessageSent(adesso);
        chat.setIsDeleted(false);

        return toResponse(chatRepository.save(chat), io.getId());
    }

    // Tutte le chat dell'utente loggato
    public List<ChatResponse> getChatDiUtente(String username) {
        Utente io = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        List<Chat> chats = chatRepository.findChatDiUtente(io.getId());
        List<ChatResponse> risultato = new ArrayList<>();
        for (Chat chat : chats) {
            risultato.add(toResponse(chat, io.getId()));
        }
        return risultato;
    }

    public void cancellaChat(UUID id) {
        Chat chat = trovaChatOppureErrore(id);
        chat.setIsDeleted(true);
        chatRepository.save(chat);
    }

    // usato dai suggerimenti IA per accumulare i token sulla chat
    public void addTokenToChat(UUID idChat, int token) {
        Chat chat = trovaChatOppureErrore(idChat);
        chat.setTokens(chat.getTokens() + token);
        chatRepository.save(chat);
    }

    private Chat trovaChatOppureErrore(UUID id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Chat non trovata con id: " + id));
    }

    // il "nome" mostrato all'utente è l'ALTRO partecipante
    private ChatResponse toResponse(Chat chat, UUID mioId) {
        String altro = chat.getUtenteUno().getId().equals(mioId)
                ? chat.getUtenteDue().getUsername()
                : chat.getUtenteUno().getUsername();
        return new ChatResponse(chat.getId(), altro, chat.getTokens(),
                chat.getCreatedAt(), chat.getLastMessageSent());
    }
}