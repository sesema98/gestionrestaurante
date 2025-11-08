package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
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

    @Column(nullable = false, length = 8, unique = true)
    private String dni;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(nullable = false, length = 60)
    private String apellidos;

    @Column(length = 15)
    private String telefono;

    @Column(length = 80)
    private String correo;

    @Column(nullable = false)
    private boolean activo = true;
}
