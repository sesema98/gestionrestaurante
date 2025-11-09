package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.services.ClienteService;
import com.serva.gestionrestaurante.services.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clientes")
@PreAuthorize("hasRole('ADMIN')")
public class ClienteController {

    private final MesaService mesaService;
    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        return "clientes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long mesaId, Model model) {
        Cliente cliente = new Cliente();
        if (mesaId != null) {
            mesaService.buscar(mesaId).ifPresent(cliente::setMesa);
        }
        prepararFormulario(model, cliente, "Registrar Cliente");
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente,
                          BindingResult result,
                          @RequestParam("mesaId") Long mesaId,
                          Model model) {

        if (mesaId == null) {
            result.rejectValue("mesa", "mesa.requerida", "Selecciona una mesa");
        }

        Mesa mesaSeleccionada = mesaId != null ? mesaService.buscar(mesaId).orElse(null) : null;
        if (mesaId != null && mesaSeleccionada == null) {
            result.rejectValue("mesa", "mesa.noExiste", "Selecciona una mesa valida");
        }
        Cliente clientePersistido = cliente.getId() != null
                ? clienteService.buscar(cliente.getId()).orElse(null)
                : null;
        Mesa mesaAnterior = clientePersistido != null ? clientePersistido.getMesa() : null;
        if (clientePersistido != null) {
            cliente.setActivo(clientePersistido.isActivo());
        }

        boolean cambiandoMesa = mesaSeleccionada != null && (mesaAnterior == null ||
                !mesaAnterior.getId().equals(mesaSeleccionada.getId()));

        if (mesaSeleccionada != null) {
            cliente.setMesa(mesaSeleccionada);
        }

        if (cambiandoMesa && mesaSeleccionada != null &&
                !"Disponible".equalsIgnoreCase(mesaSeleccionada.getEstado())) {
            result.rejectValue("mesa", "mesa.noDisponible", "La mesa no est\u00e1 disponible.");
        }

        if (result.hasErrors()) {
            prepararFormulario(model, cliente, cliente.getId() == null ? "Registrar Cliente" : "Editar Cliente");
            return "clientes/form";
        }

        if (mesaAnterior != null && cambiandoMesa) {
            mesaAnterior.setEstado("Disponible");
            mesaService.guardar(mesaAnterior);
        }

        if (mesaSeleccionada != null) {
            mesaSeleccionada.setEstado("Ocupada");
            mesaService.guardar(mesaSeleccionada);
            cliente.setMesa(mesaSeleccionada);
        }

        clienteService.guardar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var clienteOpt = clienteService.buscar(id);
        if (clienteOpt.isEmpty()) {
            return "redirect:/clientes";
        }
        prepararFormulario(model, clienteOpt.get(), "Editar Cliente");
        return "clientes/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        clienteService.buscar(id).ifPresent(c -> {
            Mesa mesa = c.getMesa();
            if (mesa != null) {
                mesa.setEstado("Disponible");
                mesaService.guardar(mesa);
            }
            clienteService.eliminar(id);
        });
        return "redirect:/clientes";
    }

    private void prepararFormulario(Model model, Cliente cliente, String titulo) {
        Long mesaActual = cliente.getMesa() != null ? cliente.getMesa().getId() : null;
        var todasLasMesas = mesaService.listar();
        var mesasDisponibles = todasLasMesas.stream()
                .filter(m -> "Disponible".equalsIgnoreCase(m.getEstado()) ||
                        (mesaActual != null && mesaActual.equals(m.getId())))
                .toList();
        var mesasActivas = todasLasMesas.stream()
                .filter(m -> !"Disponible".equalsIgnoreCase(m.getEstado()))
                .toList();
        model.addAttribute("titulo", titulo);
        model.addAttribute("cliente", cliente);
        model.addAttribute("mesasDisponibles", mesasDisponibles);
        model.addAttribute("mesasActivas", mesasActivas);
        model.addAttribute("mesaActualId", mesaActual);
    }
}
