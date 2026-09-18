package com.epicode.chatai.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String mittenteEmail;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void inviaStatistiche(String destinatario, String username,
                                 long inviati, long ricevuti, long chatAperte) {

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("messaggiInviati", inviati);
        context.setVariable("messaggiRicevuti", ricevuti);
        context.setVariable("chatAperte", chatAperte);

        String contenutoHtml = templateEngine.process("email-statistiche", context);

        try {
            MimeMessage messaggio = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(messaggio, true, "UTF-8");
            helper.setFrom(mittenteEmail);
            helper.setTo(destinatario);
            helper.setSubject("Le tue statistiche - Chat AI");
            helper.setText(contenutoHtml, true);
            mailSender.send(messaggio);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore nell'invio dell'email: " + e.getMessage());
        }
    }
}