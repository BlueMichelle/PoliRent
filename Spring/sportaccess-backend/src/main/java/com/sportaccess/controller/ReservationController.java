package com.sportaccess.controller;

import com.sportaccess.model.Court;
import com.sportaccess.model.Reservation;
import com.sportaccess.model.User;
import com.sportaccess.repository.CourtRepository;
import com.sportaccess.repository.ReservationRepository;
import com.sportaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    /** GET /api/reservations/user/{userId} — Mis reservas */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationRepository.findByUserId(userId));
    }

    /** GET /api/reservations/court/{courtId} — Reservas de una pista */
    @GetMapping("/court/{courtId}")
    public ResponseEntity<List<Reservation>> getByCourt(@PathVariable Long courtId) {
        return ResponseEntity.ok(reservationRepository.findByCourtId(courtId));
    }

    /**
     * POST /api/reservations — Crear una reserva
     * Body: { "userId": 1, "courtId": 1, "fechaInicio": "...", "fechaFin": "..." }
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long courtId = Long.valueOf(body.get("courtId").toString());
        LocalDateTime inicio = LocalDateTime.parse(body.get("fechaInicio").toString());
        LocalDateTime fin = LocalDateTime.parse(body.get("fechaFin").toString());

        // Validación: inicio debe ser antes que fin
        if (!inicio.isBefore(fin)) {
            return ResponseEntity.badRequest().body("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // Validación: no se puede reservar en el pasado
        if (inicio.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("No se puede reservar en el pasado");
        }

        // Verificar que no haya solape (anti doble reserva)
        if (reservationRepository.existeSolape(courtId, inicio, fin)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La pista ya está reservada en ese horario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Pista no encontrada"));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCourt(court);
        reservation.setFechaInicio(inicio);
        reservation.setFechaFin(fin);
        reservation.setEstado(Reservation.ReservationStatus.PENDIENTE);

        Reservation saved = reservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/reservations/validate-qr — Validar acceso con QR + GPS
     * Body: { "qrToken": "uuid", "latitud": 40.123, "longitud": -3.456 }
     */
    @PostMapping("/validate-qr")
    public ResponseEntity<?> validateQr(@RequestBody Map<String, Object> body) {
        String qrToken = body.get("qrToken").toString();
        // Latitud y longitud recibidas del GPS del móvil
        double latitud = Double.parseDouble(body.get("latitud").toString());
        double longitud = Double.parseDouble(body.get("longitud").toString());

        return reservationRepository.findByQrToken(qrToken)
                .map(reservation -> {
                    // Verificar que la reserva está CONFIRMADA
                    if (reservation.getEstado() != Reservation.ReservationStatus.CONFIRMADA) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) "La reserva no está confirmada o ya fue utilizada");
                    }

                    // Verificar franja horaria (+-15 minutos de margen)
                    LocalDateTime ahora = LocalDateTime.now();
                    LocalDateTime margenInicio = reservation.getFechaInicio().minusMinutes(15);
                    LocalDateTime margenFin = reservation.getFechaFin().plusMinutes(5);

                    if (ahora.isBefore(margenInicio) || ahora.isAfter(margenFin)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body((Object) "No estás en el horario de tu reserva");
                    }

                    // TODO: Validar geofencing con las coordenadas reales del club
                    // double distancia = GeoUtils.calcularDistancia(latitud, longitud, LAT_CLUB,
                    // LON_CLUB);
                    // if (distancia > 100) return FORBIDDEN...

                    // ¡Acceso concedido! Cambiar estado a ACTIVA
                    reservation.setEstado(Reservation.ReservationStatus.ACTIVA);
                    reservationRepository.save(reservation);

                    return ResponseEntity.ok((Object) Map.of(
                            "mensaje", "Acceso concedido. ¡Disfruta tu partido!",
                            "pista", reservation.getCourt().getNombre(),
                            "hasta", reservation.getFechaFin()));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("QR no válido"));
    }

    /** PATCH /api/reservations/{id}/cancel — Cancelar reserva */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getEstado() == Reservation.ReservationStatus.ACTIVA ||
                            reservation.getEstado() == Reservation.ReservationStatus.COMPLETADA) {
                        return ResponseEntity.badRequest()
                                .body((Object) "No se puede cancelar una reserva activa o completada");
                    }
                    reservation.setEstado(Reservation.ReservationStatus.CANCELADA);
                    return ResponseEntity.ok((Object) reservationRepository.save(reservation));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
