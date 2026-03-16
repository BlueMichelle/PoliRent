package com.sportaccess.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "courts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtType tipo;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioPorHora;

    @Column(nullable = false)
    private Boolean activa = true;

    private String descripcion;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL)
    private List<Reservation> reservas;

    public enum CourtType {
        PADEL, TENIS, FUTBOL_SALA, BALONCESTO
    }
}
