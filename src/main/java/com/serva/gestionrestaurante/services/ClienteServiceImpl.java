package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service @Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repo;

    public ClienteServiceImpl(ClienteRepository repo) { this.repo = repo; }

    @Override public List<Cliente> listar() { return repo.findAll(); }
    @Override public Optional<Cliente> buscar(Long id) { return repo.findById(id); }
    @Override public Optional<Cliente> buscarPorMesa(Long mesaId) { return repo.findFirstByMesaId(mesaId); }
    @Override public Optional<Cliente> buscarPorCorreo(String correo) { return repo.findByCorreoIgnoreCase(correo); }
    @Override public Cliente guardar(Cliente c) { return repo.save(c); }
    @Override public void eliminar(Long id) { repo.deleteById(id); }
}
