package com.serva.gestionrestaurante.services;

import com.serva.gestionrestaurante.entities.Insumo;
import com.serva.gestionrestaurante.entities.Plato;
import com.serva.gestionrestaurante.repositories.InsumoRepository;
import com.serva.gestionrestaurante.repositories.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;
    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Plato> listar() {
        return platoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plato> buscar(Long id) {
        return platoRepository.findById(id);
    }

    @Override
    public Plato guardar(Plato plato) {
        if (plato.getPrecio() == null || plato.getPrecio().doubleValue() < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a 0");
        }
        return platoRepository.save(plato);
    }

    @Override
    public void eliminar(Long id) {
        platoRepository.deleteById(id);
    }

    @Override
    public void consumirStock(Long platoId, int cantidad) {
        Plato plato = platoRepository.findById(platoId)
                .orElseThrow(() -> new IllegalArgumentException("Plato no encontrado"));
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        for (Insumo insumo : plato.getInsumos()) {
            if (insumo.getStock() - cantidad < 0) {
                throw new IllegalStateException("Stock insuficiente para " + insumo.getNombre());
            }
            insumo.setStock(insumo.getStock() - cantidad);
            insumoRepository.save(insumo);
        }
    }
}
