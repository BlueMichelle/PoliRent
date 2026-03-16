package com.sportaccess.repository;

import com.sportaccess.model.Reservation;
import com.sportaccess.model.Reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByCourtId(Long courtId);

    Optional<Reservation> findByQrToken(String qrToken);

    /**
     * Comprueba si existe una reserva que solape el intervalo de tiempo solicitado
     * para una pista concreta. Esto evita doble reserva.
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.court.id = :courtId " +
            "AND r.estado NOT IN ('CANCELADA', 'COMPLETADA') " +
            "AND r.fechaInicio < :fin " +
            "AND r.fechaFin > :inicio")
    boolean existeSolape(@Param("courtId") Long courtId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    List<Reservation> findByCourtIdAndFechaInicioBetween(Long courtId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Reservation> findByEstado(ReservationStatus estado);
}
