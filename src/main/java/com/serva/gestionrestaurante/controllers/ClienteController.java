package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.services.ClienteService;
import com.serva.gestionrestaurante.services.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private MesaService mesaService;

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        return "clientes/listar";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("titulo", "Registrar Cliente");
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("mesas", mesaService.listar().stream()
                .filter(m -> m.getEstado().equalsIgnoreCase("Disponible"))
                .toList());
        return "clientes/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cliente cliente, @RequestParam("mesaId") Long mesaId) {
        // Buscar la mesa seleccionada
        Mesa mesa = mesaService.buscar(mesaId).orElse(null);

        if (mesa != null) {
            mesa.setEstado("Ocupada");
            mesaService.guardar(mesa);
            cliente.setMesa(mesa);
        }

        clienteService.guardar(cliente);
        return "redirect:/clientes";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        var clienteOpt = clienteService.buscar(id);
        if (clienteOpt.isEmpty()) {
            return "redirect:/clientes";
        }

        var cliente = clienteOpt.get();

        // Mostrar mesas disponibles + la mesa actual del cliente
        var mesasDisponibles = mesaService.listar().stream()
                .filter(m -> m.getEstado().equalsIgnoreCase("Disponible") ||
                        (cliente.getMesa() != null && m.getId().equals(cliente.getMesa().getId())))
                .toList();

        model.addAttribute("titulo", "Editar Cliente");
        model.addAttribute("cliente", cliente);
        model.addAttribute("mesas", mesasDisponibles);

        return "clientes/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
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
}
