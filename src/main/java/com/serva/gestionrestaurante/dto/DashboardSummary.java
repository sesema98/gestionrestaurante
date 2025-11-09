package com.serva.gestionrestaurante.dto;

public record DashboardSummary(
        long totalMesas,
        long mesasDisponibles,
        long mesasOcupadas,
        long mesasReservadas,
        long clientesActivos
) {}
