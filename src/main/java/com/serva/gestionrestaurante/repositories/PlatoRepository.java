package com.serva.gestionrestaurante.repositories;

import com.serva.gestionrestaurante.entities.Plato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
