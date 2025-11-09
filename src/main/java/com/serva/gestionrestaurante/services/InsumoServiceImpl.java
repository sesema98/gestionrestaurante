package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Insumo;
import com.serva.gestionrestaurante.repositories.InsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Insumo> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Insumo> buscar(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Insumo> buscarPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByIdIn(ids);
    }

    @Override
    public Insumo guardar(Insumo insumo) {
        if (insumo.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (insumo.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        return repository.save(insumo);
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void actualizarStock(Long id, int cantidad) {
        repository.findById(id).ifPresent(insumo -> {
            if (insumo.getStock() + cantidad < 0) {
                throw new IllegalStateException("El stock no puede ser negativo");
            }
            insumo.setStock(insumo.getStock() + cantidad);
            repository.save(insumo);
        });
    }
}
