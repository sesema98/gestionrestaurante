package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.entities.Insumo;
import com.serva.gestionrestaurante.entities.Plato;
import com.serva.gestionrestaurante.services.InsumoService;
import com.serva.gestionrestaurante.services.PlatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;
    private final InsumoService insumoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MOZO','CLIENTE')")
    public String listar(Model model) {
        model.addAttribute("platos", platoService.listar());
        return "menu/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("plato", new Plato());
        model.addAttribute("insumos", insumoService.listar());
        model.addAttribute("titulo", "Registrar plato o bebida");
        return "menu/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Plato> plato = platoService.buscar(id);
        if (plato.isEmpty()) {
            flash.addFlashAttribute("error", "El plato seleccionado no existe.");
            return "redirect:/menu";
        }
        model.addAttribute("plato", plato.get());
        model.addAttribute("insumos", insumoService.listar());
        model.addAttribute("titulo", "Editar plato o bebida");
        return "menu/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute("plato") Plato plato,
                          BindingResult result,
                          @RequestParam(value = "insumosIds", required = false) List<Long> insumosIds,
                          RedirectAttributes flash,
                          Model model) {
        if (plato.getPrecio() == null) {
            plato.setPrecio(BigDecimal.ZERO);
        }
        if (result.hasErrors()) {
            model.addAttribute("insumos", insumoService.listar());
            model.addAttribute("titulo", plato.getId() == null ? "Registrar plato o bebida" : "Editar plato o bebida");
            return "menu/form";
        }

        Set<Insumo> insumosSeleccionados = new LinkedHashSet<>(insumoService.buscarPorIds(insumosIds));
        plato.actualizarInsumos(insumosSeleccionados);
        platoService.guardar(plato);
        flash.addFlashAttribute("success", "Plato guardado correctamente.");
        return "redirect:/menu";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        platoService.eliminar(id);
        flash.addFlashAttribute("success", "Plato eliminado.");
        return "redirect:/menu";
    }
}
