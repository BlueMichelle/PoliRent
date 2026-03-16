package com.sportaccess.repository;

import com.sportaccess.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByCourtId(Long courtId);

    List<Incident> findByEstado(Incident.IncidentStatus estado);

    List<Incident> findByReportadoPorId(Long userId);
}
