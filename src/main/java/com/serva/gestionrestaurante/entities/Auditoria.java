package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tabla;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Column(nullable = false, length = 20)
    private String accion; // CREATE, UPDATE, DELETE

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 80)
    private String usuario;
}
