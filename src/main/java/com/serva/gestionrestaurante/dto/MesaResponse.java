package com.serva.gestionrestaurante.dto;

import com.serva.gestionrestaurante.entities.Mesa;

public record MesaResponse(
        Long id,
        int numero,
        int capacidad,
        String estado,
        String ubicacion
) {
    public static MesaResponse fromEntity(Mesa mesa) {
        return new MesaResponse(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado(),
                mesa.getUbicacion()
        );
    }
}
