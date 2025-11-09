package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Plato;

import java.util.List;
import java.util.Optional;

public interface PlatoService {
    List<Plato> listar();
    Optional<Plato> buscar(Long id);
    Plato guardar(Plato plato);
    void eliminar(Long id);
    void consumirStock(Long platoId, int cantidad);
}
