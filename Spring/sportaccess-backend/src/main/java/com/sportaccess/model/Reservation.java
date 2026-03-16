package com.sportaccess.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus estado = ReservationStatus.PENDIENTE;

    // Token único para generar el QR de acceso
    @Column(nullable = false, unique = true)
    private String qrToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @PrePersist
    public void generarQrToken() {
        if (this.qrToken == null) {
            this.qrToken = UUID.randomUUID().toString();
        }
    }

    public enum ReservationStatus {
        PENDIENTE, // Reserva creada, pago pendiente
        CONFIRMADA, // Pago confirmado, QR activo
        ACTIVA, // Usuario ha validado entrada (GPS + QR)
        COMPLETADA, // Sesión finalizada
        CANCELADA // Reserva cancelada
    }
}
