package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.dto.*;
import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.services.ClienteService;
import com.serva.gestionrestaurante.services.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardRestController {

    private final MesaService mesaService;
    private final ClienteService clienteService;

    @GetMapping("/mesas/{id}")
    public MesaDetailResponse detalleMesa(@PathVariable Long id, Authentication authentication) {
        Mesa mesa = mesaService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
        Cliente cliente = clienteService.buscarPorMesa(id).orElse(null);

        if (!tieneRol(authentication, "ROLE_ADMIN")) {
            if (cliente == null || cliente.getCorreo() == null ||
                    !cliente.getCorreo().equalsIgnoreCase(authentication.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos sobre esta mesa");
            }
        }

        return new MesaDetailResponse(MesaResponse.fromEntity(mesa), ClienteResponse.fromEntity(cliente));
    }

    @PostMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MesaDetailResponse> guardarCliente(@Valid @RequestBody ClienteRequest request) {
        Mesa mesa = mesaService.buscar(request.mesaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La mesa no existe"));

        Cliente cliente = request.id() != null
                ? clienteService.buscar(request.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"))
                : new Cliente();

        Mesa mesaAnterior = cliente.getMesa();
        boolean cambiandoMesa = mesaAnterior == null || !mesaAnterior.getId().equals(mesa.getId());
        if (cambiandoMesa && !estaDisponible(mesa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La mesa seleccionada no esta disponible");
        }

        if (mesaAnterior != null && cambiandoMesa) {
            mesaAnterior.setEstado("Disponible");
            mesaService.guardar(mesaAnterior);
        }

        cliente.setDni(request.dni());
        cliente.setNombres(request.nombres());
        cliente.setApellidos(request.apellidos());
        cliente.setTelefono(request.telefono());
        cliente.setCorreo(request.correo());
        cliente.setActivo(true);
        cliente.setMesa(mesa);

        Cliente guardado = clienteService.guardar(cliente);
        mesa.setEstado("Ocupada");
        mesaService.guardar(mesa);

        return ResponseEntity.ok(new MesaDetailResponse(
                MesaResponse.fromEntity(mesa),
                ClienteResponse.fromEntity(guardado)
        ));
    }

    @DeleteMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        Cliente cliente = clienteService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        Mesa mesa = cliente.getMesa();
        clienteService.eliminar(id);
        if (mesa != null) {
            mesa.setEstado("Disponible");
            mesaService.guardar(mesa);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mesas/{id}/liberar")
    @PreAuthorize("hasRole('ADMIN')")
    public MesaDetailResponse liberarMesa(@PathVariable Long id) {
        Mesa mesa = mesaService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
        clienteService.buscarPorMesa(id).ifPresent(cliente -> {
            cliente.setMesa(null);
            clienteService.guardar(cliente);
        });
        mesa.setEstado("Disponible");
        mesaService.guardar(mesa);

        return new MesaDetailResponse(MesaResponse.fromEntity(mesa), null);
    }

    @RequestMapping(value = "/mesas/{id}/estado", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public MesaDetailResponse actualizarEstadoMesa(@PathVariable Long id,
                                                   @RequestBody MesaEstadoRequest request) {
        Mesa mesa = mesaService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
        String estadoNormalizado = normalizarEstado(request.estado());
        if (estadoNormalizado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido");
        }

        if ("Disponible".equalsIgnoreCase(estadoNormalizado)) {
            clienteService.buscarPorMesa(id).ifPresent(cliente -> {
                cliente.setMesa(null);
                clienteService.guardar(cliente);
            });
        }

        mesa.setEstado(estadoNormalizado);
        mesaService.guardar(mesa);
        Cliente cliente = clienteService.buscarPorMesa(id).orElse(null);
        return new MesaDetailResponse(MesaResponse.fromEntity(mesa), ClienteResponse.fromEntity(cliente));
    }

    @RequestMapping(value = "/clientes/{id}/mesa", method = {RequestMethod.PATCH, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public MesaDetailResponse asignarClienteMesa(@PathVariable Long id,
                                                 @RequestBody AsignacionMesaRequest request) {
        if (request.mesaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesa requerida");
        }
        Cliente cliente = clienteService.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        Mesa mesa = mesaService.buscar(request.mesaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));

        if (cliente.getMesa() != null && !cliente.getMesa().getId().equals(mesa.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cliente ya tiene una mesa asignada");
        }

        if (!"Disponible".equalsIgnoreCase(mesa.getEstado()) &&
                (cliente.getMesa() == null || !mesa.getId().equals(cliente.getMesa().getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La mesa no está disponible");
        }

        Mesa mesaAnterior = cliente.getMesa();
        if (mesaAnterior != null && !mesaAnterior.getId().equals(mesa.getId())) {
            mesaAnterior.setEstado("Disponible");
            mesaService.guardar(mesaAnterior);
        }

        cliente.setMesa(mesa);
        clienteService.guardar(cliente);
        mesa.setEstado("Ocupada");
        mesaService.guardar(mesa);
        return new MesaDetailResponse(MesaResponse.fromEntity(mesa), ClienteResponse.fromEntity(cliente));
    }

    private boolean tieneRol(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private boolean estaDisponible(Mesa mesa) {
        return "Disponible".equalsIgnoreCase(mesa.getEstado());
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
