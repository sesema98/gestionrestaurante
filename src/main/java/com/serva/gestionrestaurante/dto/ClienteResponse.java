package com.serva.gestionrestaurante.dto;

import com.serva.gestionrestaurante.entities.Cliente;

public record ClienteResponse(
        Long id,
        String dni,
        String nombres,
        String apellidos,
        String telefono,
        String correo,
        MesaResponse mesa
) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return new ClienteResponse(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getMesa() != null ? MesaResponse.fromEntity(cliente.getMesa()) : null
        );
    }
}
