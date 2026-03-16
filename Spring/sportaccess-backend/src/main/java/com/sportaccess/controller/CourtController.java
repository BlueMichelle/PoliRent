package com.sportaccess.controller;

import com.sportaccess.model.Court;
import com.sportaccess.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtRepository courtRepository;

    /** GET /api/courts — Lista todas las pistas activas */
    @GetMapping
    public ResponseEntity<List<Court>> getAllActiveCourts() {
        return ResponseEntity.ok(courtRepository.findByActivaTrue());
    }

    /** GET /api/courts/all — Lista TODAS las pistas (admin) */
    @GetMapping("/all")
    public ResponseEntity<List<Court>> getAllCourts() {
        return ResponseEntity.ok(courtRepository.findAll());
    }

    /** GET /api/courts/{id} — Detalle de una pista */
    @GetMapping("/{id}")
    public ResponseEntity<Court> getCourtById(@PathVariable Long id) {
        return courtRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/courts — Crear nueva pista (Admin) */
    @PostMapping
    public ResponseEntity<Court> createCourt(@Valid @RequestBody Court court) {
        Court saved = courtRepository.save(court);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** PUT /api/courts/{id} — Actualizar pista (Admin) */
    @PutMapping("/{id}")
    public ResponseEntity<Court> updateCourt(@PathVariable Long id,
            @Valid @RequestBody Court courtData) {
        return courtRepository.findById(id)
                .map(court -> {
                    court.setNombre(courtData.getNombre());
                    court.setTipo(courtData.getTipo());
                    court.setPrecioPorHora(courtData.getPrecioPorHora());
                    court.setActiva(courtData.getActiva());
                    court.setDescripcion(courtData.getDescripcion());
                    return ResponseEntity.ok(courtRepository.save(court));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/courts/{id} — Desactivar pista (no borrar) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCourt(@PathVariable Long id) {
        return courtRepository.findById(id)
                .map(court -> {
                    court.setActiva(false);
                    courtRepository.save(court);
                    return ResponseEntity.<Void>ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
