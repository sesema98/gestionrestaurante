package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = true)
    private Mesa mesa;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 8)
    @Column(nullable = false, length = 8, unique = true)
    private String dni;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String nombres;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String apellidos;

    @Size(max = 15)
    @Column(length = 15)
    private String telefono;

    @Email
    @Size(max = 80)
    @Column(length = 80)
    private String correo;

    @Column(nullable = false)
    private boolean activo = true;
}
