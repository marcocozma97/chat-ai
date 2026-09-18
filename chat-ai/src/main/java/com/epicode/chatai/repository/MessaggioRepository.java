package com.epicode.chatai.repository;

import com.epicode.chatai.entity.Messaggio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MessaggioRepository extends JpaRepository<Messaggio, UUID> {

    // Messaggi di una chat
    Page<Messaggio> findByChat_IdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    // Messaggi INVIATI da un utente
    long countByMittente_Id(UUID mittenteId);

    // Messaggi RICEVUTI
    @Query("SELECT COUNT(m) FROM Messaggio m WHERE " +
            "(m.chat.utenteUno.id = :userId OR m.chat.utenteDue.id = :userId) " +
            "AND m.mittente.id <> :userId")
    long countMessaggiRicevuti(@Param("userId") UUID userId);
}