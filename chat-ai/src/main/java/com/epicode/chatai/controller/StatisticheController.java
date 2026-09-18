package com.epicode.chatai.controller;

import com.epicode.chatai.dto.StatisticheResponse;
import com.epicode.chatai.service.StatisticheService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/statistiche")
public class StatisticheController {

    private final StatisticheService statisticheService;

    public StatisticheController(StatisticheService statisticheService) {
        this.statisticheService = statisticheService;
    }

    @PostMapping("/invia")
    public StatisticheResponse inviaStatistiche(Principal principal) {
        return statisticheService.calcolaEInvia(principal.getName());
    }
}