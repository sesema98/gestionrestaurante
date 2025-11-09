package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Insumo;

import java.util.List;
import java.util.Optional;

public interface InsumoService {
    List<Insumo> listar();
    Optional<Insumo> buscar(Long id);
    List<Insumo> buscarPorIds(List<Long> ids);
    Insumo guardar(Insumo insumo);
    void eliminar(Long id);
    void actualizarStock(Long id, int cantidad);
}
