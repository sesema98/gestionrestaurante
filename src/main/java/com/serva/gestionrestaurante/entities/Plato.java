package com.serva.gestionrestaurante.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "platos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    @jakarta.validation.constraints.DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal precio;

    @ManyToMany
    @JoinTable(name = "plato_insumos",
            joinColumns = @JoinColumn(name = "plato_id"),
            inverseJoinColumns = @JoinColumn(name = "insumo_id"))
    @Builder.Default
    private Set<Insumo> insumos = new LinkedHashSet<>();

    public void actualizarInsumos(Set<Insumo> nuevos) {
        insumos.clear();
        if (nuevos != null) {
            insumos.addAll(nuevos);
        }
    }
}
