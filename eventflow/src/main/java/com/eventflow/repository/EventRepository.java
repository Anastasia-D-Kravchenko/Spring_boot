package com.eventflow.repository;

import com.eventflow.model.Event;
import com.eventflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByStatusOrderByEventDateAsc(Event.Status status, Pageable pageable);

    Page<Event> findByCategoryAndStatusOrderByEventDateAsc(Event.Category category, Event.Status status, Pageable pageable);

    List<Event> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    Page<Event> findByCreatedByOrderByCreatedAtDesc(User createdBy, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND (" +
            "LOWER(e.title)       LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.location)    LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Event> searchEvents(@Param("query") String query, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND e.eventDate >= :fromDate ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingEvents(@Param("fromDate") LocalDate fromDate, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND e.eventDate BETWEEN :from AND :to ORDER BY e.eventDate ASC")
    List<Event> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByStatus(Event.Status status);

    long countByCategory(Event.Category category);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventDate >= :today AND e.status = 'ACTIVE'")
    long countUpcoming(@Param("today") LocalDate today);

    @Query("SELECT e.category, COUNT(e) FROM Event e GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> countByCategory();
}
