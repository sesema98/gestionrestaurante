package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.entities.Insumo;
import com.serva.gestionrestaurante.services.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/insumos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("insumos", insumoService.listar());
        return "insumos/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("insumo", new Insumo());
        model.addAttribute("titulo", "Registrar insumo");
        return "insumos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Insumo> insumo = insumoService.buscar(id);
        if (insumo.isEmpty()) {
            flash.addFlashAttribute("error", "El insumo indicado no existe.");
            return "redirect:/insumos";
        }
        model.addAttribute("insumo", insumo.get());
        model.addAttribute("titulo", "Editar insumo");
        return "insumos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("insumo") Insumo insumo,
                          BindingResult result,
                          RedirectAttributes flash,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", insumo.getId() == null ? "Registrar insumo" : "Editar insumo");
            return "insumos/form";
        }
        insumoService.guardar(insumo);
        flash.addFlashAttribute("success", "Insumo guardado correctamente.");
        return "redirect:/insumos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        insumoService.eliminar(id);
        flash.addFlashAttribute("success", "Insumo eliminado.");
        return "redirect:/insumos";
    }
}
