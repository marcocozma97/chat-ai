package com.epicode.chatai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatisticheResponse {
    private long messaggiInviati;
    private long messaggiRicevuti;
    private long chatAperte;
}