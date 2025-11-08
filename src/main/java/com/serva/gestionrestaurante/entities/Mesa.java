package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mesas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false, length = 20)
    private String estado; // Disponible, Ocupada, Reservada

    @Column(length = 40)
    private String ubicacion;
}
