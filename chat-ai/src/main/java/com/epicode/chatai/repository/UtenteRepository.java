package com.epicode.chatai.repository;

import com.epicode.chatai.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UtenteRepository extends JpaRepository<Utente, UUID> {

    Optional<Utente> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}