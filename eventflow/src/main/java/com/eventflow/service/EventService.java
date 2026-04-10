package com.eventflow.service;

import com.eventflow.dto.EventFlowDtos.*;
import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public Event createEvent(EventCreateDto dto, User createdBy) {
        Event event = new Event(
                dto.getTitle(),
                dto.getDescription(),
                dto.getEventDate(),
                dto.getEventTime(),
                dto.getLocation(),
                dto.getMaxParticipants(),
                dto.getCategory(),
                createdBy
        );
        event.setPrice(dto.getPrice());
        event.setImageUrl(dto.getImageUrl());
        event.setAdditionalInfo(dto.getAdditionalInfo());
        event.setRequiresApproval(dto.isRequiresApproval());

        Event saved = eventRepository.save(event);
        log.info("Event created: '{}' by {}", saved.getTitle(), createdBy.getEmail());
        return saved;
    }

    // Create from AI-extracted map (AI command handler calls this)
    @Transactional
    public Event createEventFromMap(java.util.Map<String, Object> data, User createdBy) {
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle(getString(data, "title", "Untitled Event"));
        dto.setDescription(getString(data, "description", "No description provided."));
        dto.setLocation(getString(data, "location", "TBD"));
        dto.setMaxParticipants(getInt(data, "maxParticipants", 50));

        // Parse date and time with sensible defaults
        String dateStr = getString(data, "date", "");
        String timeStr = getString(data, "time", "12:00");
        dto.setEventDate(dateStr.isBlank() ? LocalDate.now().plusDays(7) : LocalDate.parse(dateStr));
        dto.setEventTime(timeStr.isBlank() ? LocalTime.NOON : LocalTime.parse(timeStr));

        String catStr = getString(data, "category", "OTHER").toUpperCase();
        try { dto.setCategory(Event.Category.valueOf(catStr)); }
        catch (Exception e) { dto.setCategory(Event.Category.OTHER); }

        dto.setPrice(getDouble(data, "price", 0.0));

        return createEvent(dto, createdBy);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public Event updateEvent(Long id, EventCreateDto dto, User requestingUser) {
        Event event = findByIdOrThrow(id);
        checkOwnership(event, requestingUser);

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setEventTime(dto.getEventTime());
        event.setLocation(dto.getLocation());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setCategory(dto.getCategory());
        event.setPrice(dto.getPrice());
        if (dto.getImageUrl() != null) event.setImageUrl(dto.getImageUrl());
        event.setAdditionalInfo(dto.getAdditionalInfo());

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateStatus(Long id, Event.Status status, User requestingUser) {
        Event event = findByIdOrThrow(id);
        checkOwnership(event, requestingUser);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void deleteEvent(Long id, User requestingUser) {
        Event event = findByIdOrThrow(id);
        checkOwnership(event, requestingUser);
        eventRepository.delete(event);
        log.info("Event '{}' deleted by {}", event.getTitle(), requestingUser.getEmail());
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event findByIdOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Event> findUpcoming(int page, int size) {
        return eventRepository.findUpcomingEvents(LocalDate.now(),
                PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Event> findByCategory(Event.Category category, int page, int size) {
        return eventRepository.findByCategoryAndStatusOrderByEventDateAsc(
                category, Event.Status.ACTIVE,
                PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Event> searchEvents(String query, int page, int size) {
        return eventRepository.searchEvents(query, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Event> findByCreator(User user, int page, int size) {
        return eventRepository.findByCreatedByOrderByCreatedAtDesc(
                user, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<Event> findAllActiveByCreator(User user) {
        return eventRepository.findByCreatedByOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Event> findByDateRange(LocalDate from, LocalDate to) {
        return eventRepository.findByDateRange(from, to);
    }

    public long countTotal() {
        return eventRepository.count();
    }

    public long countUpcoming() {
        return eventRepository.countUpcoming(LocalDate.now());
    }

    public long countByStatus(Event.Status status) {
        return eventRepository.countByStatus(status);
    }

    public List<Object[]> getCategoryStats() {
        return eventRepository.countByCategory();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void checkOwnership(Event event, User user) {
        if (user.getRole() == User.Role.ADMIN) return; // admin can do anything
        if (!event.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to modify this event");
        }
    }

    private String getString(java.util.Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }

    private int getInt(java.util.Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val == null) return def;
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return def; }
    }

    private double getDouble(java.util.Map<String, Object> map, String key, double def) {
        Object val = map.get(key);
        if (val == null) return def;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return def; }
    }
}
