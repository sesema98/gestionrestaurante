package com.serva.gestionrestaurante.repositories;

import com.serva.gestionrestaurante.entities.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Insumo> findByIdIn(List<Long> ids);
}
