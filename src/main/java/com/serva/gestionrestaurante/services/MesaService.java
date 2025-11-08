package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Mesa;

import java.util.List;
import java.util.Optional;

public interface MesaService {
    List<Mesa> listar();
    Optional<Mesa> buscar(Long id);
    Mesa guardar(Mesa m);
    void eliminar(Long id);
}
