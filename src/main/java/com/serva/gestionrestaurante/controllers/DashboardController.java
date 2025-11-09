package com.serva.gestionrestaurante.controllers;

import com.serva.gestionrestaurante.dto.DashboardSummary;
import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.services.ClienteService;
import com.serva.gestionrestaurante.services.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MesaService mesaService;
    private final ClienteService clienteService;

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        var mesas = mesaService.listar();
        var clientes = clienteService.listar();

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isMozo = hasRole(authentication, "ROLE_MOZO");
        boolean canManage = isAdmin || isMozo;
        List<Cliente> visibles = clientes;
        Cliente reservaActual = null;

        if (!canManage && authentication != null) {
            Optional<Cliente> reserva = clienteService.buscarPorCorreo(authentication.getName());
            reservaActual = reserva.orElse(null);
            visibles = reserva.map(List::of).orElseGet(Collections::emptyList);
        }

        DashboardSummary summary = new DashboardSummary(
                mesas.size(),
                mesas.stream().filter(m -> "Disponible".equalsIgnoreCase(m.getEstado())).count(),
                mesas.stream().filter(m -> "Ocupada".equalsIgnoreCase(m.getEstado())).count(),
                mesas.stream().filter(m -> "Reservada".equalsIgnoreCase(m.getEstado())).count(),
                clientes.stream().filter(Cliente::isActivo).count()
        );

        model.addAttribute("mesas", mesas);
        model.addAttribute("clientes", visibles);
        model.addAttribute("summary", summary);
        model.addAttribute("clienteForm", new Cliente());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canManage", canManage);
        model.addAttribute("usuarioActivo", authentication != null ? authentication.getName() : "");
        model.addAttribute("reservaActual", reservaActual);
        return "dashboard";
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
