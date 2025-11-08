package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> listar();
    Optional<Cliente> buscar(Long id);
    Cliente guardar(Cliente c);
    void eliminar(Long id);
}
