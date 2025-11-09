package com.serva.gestionrestaurante.repositories;

import com.serva.gestionrestaurante.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findFirstByMesaId(Long mesaId);
    Optional<Cliente> findByCorreoIgnoreCase(String correo);
}
