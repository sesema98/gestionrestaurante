package com.serva.gestionrestaurante.repositories;

import com.serva.gestionrestaurante.entities.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumero(int numero);
}
