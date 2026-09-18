package com.epicode.chatai.service;

import com.epicode.chatai.entity.Utente;
import com.epicode.chatai.repository.UtenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registra(String username, String email, String password) {
        if (utenteRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username già in uso");
        }
        if (utenteRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email già in uso");
        }

        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(password));
        utente.setRuolo("ROLE_USER");

        utenteRepository.save(utente);
    }
}