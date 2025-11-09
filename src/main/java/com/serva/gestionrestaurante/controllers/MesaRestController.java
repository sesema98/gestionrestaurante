package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.dto.ClienteResponse;
import com.serva.gestionrestaurante.dto.MesaDetailResponse;
import com.serva.gestionrestaurante.dto.MesaEstadoRequest;
import com.serva.gestionrestaurante.dto.MesaResponse;
import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.services.ClienteService;
import com.serva.gestionrestaurante.services.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaRestController {

    private final MesaService mesaService;
    private final ClienteService clienteService;

    @RequestMapping(value = "/{id}/estado", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasAnyRole('ADMIN','MOZO')")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody MesaEstadoRequest request) {
        return mesaService.buscar(id)
                .map(mesa -> {
                    String nuevoEstado = normalizarEstado(request.estado());
                    if (nuevoEstado == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Estado no válido"));
                    }

                    if ("Disponible".equalsIgnoreCase(nuevoEstado)) {
                        clienteService.buscarPorMesa(id).ifPresent(cliente -> {
                            cliente.setMesa(null);
                            clienteService.guardar(cliente);
                        });
                    }

                    mesa.setEstado(nuevoEstado);
                    mesaService.guardar(mesa);
                    var cliente = clienteService.buscarPorMesa(id).orElse(null);

                    return ResponseEntity.ok(new MesaDetailResponse(
                            MesaResponse.fromEntity(mesa),
                            ClienteResponse.fromEntity(cliente)
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Mesa no encontrada")));
    }

    private String normalizarEstado(String estado) {
        if (estado == null) return null;
        return switch (estado.trim().toLowerCase()) {
            case "disponible" -> "Disponible";
            case "ocupada" -> "Ocupada";
            case "reservada" -> "Reservada";
            default -> null;
        };
    }
}
