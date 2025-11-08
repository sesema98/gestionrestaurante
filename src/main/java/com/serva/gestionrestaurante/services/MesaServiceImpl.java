package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.repositories.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MesaServiceImpl implements MesaService {

    private final MesaRepository repo;

    public MesaServiceImpl(MesaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Mesa> listar() { return repo.findAll(); }

    @Override
    public Optional<Mesa> buscar(Long id) { return repo.findById(id); }

    @Override
    public Mesa guardar(Mesa m) { return repo.save(m); }

    @Override
    public void eliminar(Long id) { repo.deleteById(id); }
}
