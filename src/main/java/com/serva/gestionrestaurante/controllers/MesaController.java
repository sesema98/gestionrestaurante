package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.entities.Mesa;
import com.serva.gestionrestaurante.services.MesaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/mesas")
@PreAuthorize("hasRole('ADMIN')")
public class MesaController {

    private MesaService service;

    public MesaController(MesaService service) {
        this.service = service;
    }

    @GetMapping("/mapa")
    public String mapa(Model model) {
        model.addAttribute("mesas", service.listar());
        return "mesas/mapa";
    }


    @GetMapping
    public String listar(Model model) {
        model.addAttribute("titulo", "Mesas");
        model.addAttribute("mesas", service.listar());
        return "mesas/listar";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("titulo", "Nueva Mesa");
        model.addAttribute("mesa", new Mesa());
        return "mesas/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Mesa mesa, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva Mesa");
            return "mesas/form";
        }
        service.guardar(mesa);
        return "redirect:/mesas";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<Mesa> mesa = service.buscar(id);
        if (mesa.isEmpty()) {
            return "redirect:/mesas";
        }
        model.addAttribute("titulo", "Editar Mesa");
        model.addAttribute("mesa", mesa.get());
        return "mesas/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/mesas";
    }
}
