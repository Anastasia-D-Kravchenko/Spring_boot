package com.eventflow.service;

import com.eventflow.model.*;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Registration registerForEvent(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (event.getStatus() != Event.Status.ACTIVE) {
            throw new IllegalStateException("This event is not accepting registrations");
        }
        if (event.isPast()) {
            throw new IllegalStateException("Cannot register for a past event");
        }
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("You are already registered for this event");
        }
        if (event.isFull()) {
            throw new IllegalStateException("This event is full (all " + event.getMaxParticipants() + " spots are taken)");
        }

        Registration reg = new Registration(user, event);
        reg.setStatus(event.isRequiresApproval()
                ? Registration.RegistrationStatus.PENDING
                : Registration.RegistrationStatus.CONFIRMED);

        Registration saved = registrationRepository.save(reg);
        log.info("User {} registered for event '{}'", user.getEmail(), event.getTitle());
        return saved;
    }

    @Transactional
    public void cancelRegistration(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        Registration reg = registrationRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new IllegalStateException("You are not registered for this event"));

        reg.setStatus(Registration.RegistrationStatus.CANCELLED);
        reg.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(reg);
        log.info("User {} cancelled registration for event '{}'", user.getEmail(), event.getTitle());
    }

    @Transactional(readOnly = true)
    public List<Registration> getUserRegistrations(User user) {
        return registrationRepository.findConfirmedWithEvents(user);
    }

    @Transactional(readOnly = true)
    public List<Registration> getEventRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        return registrationRepository.findByEvent(event);
    }

    public boolean isRegistered(User user, Event event) {
        return registrationRepository.existsByUserAndEvent(user, event);
    }

    public long countConfirmedForEvent(Event event) {
        return registrationRepository.countByEventAndStatus(event, Registration.RegistrationStatus.CONFIRMED);
    }

    public long countTotalRegistrations() {
        return registrationRepository.count();
    }
}
