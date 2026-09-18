package com.epicode.chatai.service;

import com.epicode.chatai.dto.StatisticheResponse;
import com.epicode.chatai.entity.Utente;
import com.epicode.chatai.repository.ChatRepository;
import com.epicode.chatai.repository.MessaggioRepository;
import com.epicode.chatai.repository.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StatisticheService {

    private final MessaggioRepository messaggioRepository;
    private final ChatRepository chatRepository;
    private final UtenteRepository utenteRepository;
    private final EmailService emailService;

    public StatisticheService(MessaggioRepository messaggioRepository,
                              ChatRepository chatRepository,
                              UtenteRepository utenteRepository,
                              EmailService emailService) {
        this.messaggioRepository = messaggioRepository;
        this.chatRepository = chatRepository;
        this.utenteRepository = utenteRepository;
        this.emailService = emailService;
    }

    public StatisticheResponse calcolaEInvia(String username) {
        Utente utente = utenteRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utente non trovato: " + username));

        long inviati = messaggioRepository.countByMittente_Id(utente.getId());
        long ricevuti = messaggioRepository.countMessaggiRicevuti(utente.getId());
        long chatAperte = chatRepository.countChatAperteDiUtente(utente.getId());

        emailService.inviaStatistiche(utente.getEmail(), utente.getUsername(),
                inviati, ricevuti, chatAperte);

        return new StatisticheResponse(inviati, ricevuti, chatAperte);
    }
}