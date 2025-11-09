package com.serva.gestionrestaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        Long id,
        @NotBlank @Size(max = 8) String dni,
        @NotBlank @Size(max = 60) String nombres,
        @NotBlank @Size(max = 60) String apellidos,
        @Size(max = 15) String telefono,
        @Email @Size(max = 80) String correo,
        @NotNull Long mesaId
) {}
