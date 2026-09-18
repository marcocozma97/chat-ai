package com.epicode.chatai.service;

import com.epicode.chatai.dto.MessaggioResponse;
import com.epicode.chatai.dto.RispostaLLM;
import com.epicode.chatai.entity.Chat;
import com.epicode.chatai.entity.Messaggio;
import com.epicode.chatai.entity.Utente;
import com.epicode.chatai.repository.ChatRepository;
import com.epicode.chatai.repository.MessaggioRepository;
import com.epicode.chatai.repository.UtenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class MessaggioService {

    private final MessaggioRepository messaggioRepository;
    private final ChatRepository chatRepository;
    private final UtenteRepository utenteRepository;
    private final ChatService chatService;
    private final OpenRouterService openRouterService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessaggioService(MessaggioRepository messaggioRepository,
                            ChatRepository chatRepository,
                            UtenteRepository utenteRepository,
                            ChatService chatService,
                            OpenRouterService openRouterService,
                            SimpMessagingTemplate messagingTemplate) {
        this.messaggioRepository = messaggioRepository;
        this.chatRepository = chatRepository;
        this.utenteRepository = utenteRepository;
        this.chatService = chatService;
        this.openRouterService = openRouterService;
        this.messagingTemplate = messagingTemplate;
    }

    public MessaggioResponse nuovoMessaggio(String usernameMittente, UUID idChat, String testo) {
        Chat chat = chatRepository.findById(idChat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat non trovata"));

        Utente mittente = utenteRepository.findByUsername(usernameMittente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));

        boolean partecipante = chat.getUtenteUno().getId().equals(mittente.getId())
                || chat.getUtenteDue().getId().equals(mittente.getId());
        if (!partecipante) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non fai parte di questa chat");
        }

        Messaggio messaggio = new Messaggio();
        messaggio.setMittente(mittente);
        messaggio.setTesto(testo);
        messaggio.setCreatedAt(Instant.now());
        messaggio.setChat(chat);
        Messaggio salvato = messaggioRepository.save(messaggio);

        chat.setLastMessageSent(Instant.now());
        chatRepository.save(chat);

        MessaggioResponse risposta = toResponse(salvato);

        messagingTemplate.convertAndSend("/topic/chat/" + idChat, risposta);

        return risposta;
    }

    public Page<MessaggioResponse> getMessaggiByChat(UUID chatId, int pagina) {
        Pageable pageable = PageRequest.of(pagina, 50);
        return messaggioRepository.findByChat_IdOrderByCreatedAtDesc(chatId, pageable)
                .map(this::toResponse);
    }

    public String proponiRisposta(UUID idChat, String messaggioRicevuto) {
        String prompt = "In una chat ho ricevuto questo messaggio: \"" + messaggioRicevuto + "\". "
                + "Proponimi una possibile risposta breve e naturale in italiano, "
                + "senza aggiungere spiegazioni o virgolette.";
        RispostaLLM risposta = openRouterService.chiedi(prompt);
        chatService.addTokenToChat(idChat, risposta.getTokenTotali());
        return risposta.getTesto();
    }

    private MessaggioResponse toResponse(Messaggio m) {
        return new MessaggioResponse(
                m.getId(),
                m.getMittente().getUsername(),
                m.getTesto(),
                m.getCreatedAt()
        );
    }
}