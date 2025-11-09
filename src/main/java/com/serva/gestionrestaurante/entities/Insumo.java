package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String unidadMedida;

    @ManyToMany(mappedBy = "insumos")
    @Builder.Default
    private Set<Plato> platos = new HashSet<>();

    public void reducirStock(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (stock - cantidad < 0) {
            throw new IllegalStateException("Stock insuficiente para " + nombre);
        }
        stock -= cantidad;
    }
}
