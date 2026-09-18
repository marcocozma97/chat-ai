package com.epicode.chatai.repository;

import com.epicode.chatai.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    // Tutte le chat non cancellate in cui l'utente è uno dei due partecipanti
    @Query("SELECT c FROM Chat c WHERE c.isDeleted = false " +
            "AND (c.utenteUno.id = :userId OR c.utenteDue.id = :userId) " +
            "ORDER BY c.lastMessageSent DESC")
    List<Chat> findChatDiUtente(@Param("userId") UUID userId);

    // Cerca una chat già esistente tra due utenti (in qualsiasi ordine)
    @Query("SELECT c FROM Chat c WHERE " +
            "(c.utenteUno.id = :a AND c.utenteDue.id = :b) OR " +
            "(c.utenteUno.id = :b AND c.utenteDue.id = :a)")
    Optional<Chat> findChatTraDueUtenti(@Param("a") UUID a, @Param("b") UUID b);

    // Conta le chat aperte (non cancellate) di un utente
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.isDeleted = false " +
            "AND (c.utenteUno.id = :userId OR c.utenteDue.id = :userId)")
    long countChatAperteDiUtente(@Param("userId") UUID userId);
}