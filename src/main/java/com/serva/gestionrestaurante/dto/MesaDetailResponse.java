package com.serva.gestionrestaurante.dto;

public record MesaDetailResponse(
        MesaResponse mesa,
        ClienteResponse cliente
) {}
