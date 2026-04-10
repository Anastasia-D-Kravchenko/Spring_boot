package com.eventflow.repository;

import com.eventflow.model.Event;
import com.eventflow.model.Registration;
import com.eventflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    List<Registration> findByUser(User user);

    List<Registration> findByEvent(Event event);

    List<Registration> findByUserAndStatus(User user, Registration.RegistrationStatus status);

    long countByEventAndStatus(Event event, Registration.RegistrationStatus status);

    @Query("SELECT r FROM Registration r JOIN FETCH r.event WHERE r.user = :user AND r.status = 'CONFIRMED' ORDER BY r.event.eventDate ASC")
    List<Registration> findConfirmedWithEvents(@Param("user") User user);

    long countByUser(User user);

    long countByStatus(Registration.RegistrationStatus status);
}
