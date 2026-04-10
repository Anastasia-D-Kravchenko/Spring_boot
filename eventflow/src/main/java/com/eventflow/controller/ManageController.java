package com.eventflow.controller;

import com.eventflow.dto.EventFlowDtos.*;
import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.security.SecurityUtils;
import com.eventflow.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manage")
@PreAuthorize("hasAnyRole('DATA_ADDER', 'ADMIN')")
@RequiredArgsConstructor
public class ManageController {

    private final EventService eventService;
    private final AiCommandService aiCommandService;
    private final RegistrationService registrationService;
    private final SecurityUtils securityUtils;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = securityUtils.getCurrentUserOrThrow();
        model.addAttribute("user", user);
        model.addAttribute("myEvents", eventService.findByCreator(user, 0, 10));
        model.addAttribute("totalMyEvents", eventService.findAllActiveByCreator(user).size());
        model.addAttribute("aiHistory", aiCommandService.getHistory(user));
        return "manage/dashboard";
    }

    // ─── Event CRUD ───────────────────────────────────────────────────────────

    @GetMapping("/events")
    public String myEvents(@RequestParam(defaultValue = "0") int page, Model model) {
        User user = securityUtils.getCurrentUserOrThrow();
        model.addAttribute("events", eventService.findByCreator(user, page, 12));
        model.addAttribute("currentPage", page);
        model.addAttribute("user", user);
        return "manage/events";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("dto", new EventCreateDto());
        model.addAttribute("categories", Event.Category.values());
        model.addAttribute("user", securityUtils.getCurrentUserOrThrow());
        return "manage/event-form";
    }

    @PostMapping("/events/new")
    public String createEvent(@Valid @ModelAttribute("dto") EventCreateDto dto,
                               BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Event.Category.values());
            model.addAttribute("user", securityUtils.getCurrentUserOrThrow());
            return "manage/event-form";
        }
        User user = securityUtils.getCurrentUserOrThrow();
        Event event = eventService.createEvent(dto, user);
        ra.addFlashAttribute("success", "Event '" + event.getTitle() + "' created successfully!");
        return "redirect:/manage/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        User user = securityUtils.getCurrentUserOrThrow();
        Event event = eventService.findByIdOrThrow(id);
        // Populate DTO from event
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setLocation(event.getLocation());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setCategory(event.getCategory());
        dto.setPrice(event.getPrice());
        dto.setImageUrl(event.getImageUrl());
        dto.setAdditionalInfo(event.getAdditionalInfo());
        dto.setRequiresApproval(event.isRequiresApproval());

        model.addAttribute("dto", dto);
        model.addAttribute("event", event);
        model.addAttribute("categories", Event.Category.values());
        model.addAttribute("user", user);
        return "manage/event-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                               @Valid @ModelAttribute("dto") EventCreateDto dto,
                               BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Event.Category.values());
            model.addAttribute("user", securityUtils.getCurrentUserOrThrow());
            return "manage/event-form";
        }
        User user = securityUtils.getCurrentUserOrThrow();
        eventService.updateEvent(id, dto, user);
        ra.addFlashAttribute("success", "Event updated.");
        return "redirect:/manage/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        User user = securityUtils.getCurrentUserOrThrow();
        try {
            eventService.deleteEvent(id, user);
            ra.addFlashAttribute("success", "Event deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage/events";
    }

    @GetMapping("/events/{id}/registrations")
    public String eventRegistrations(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findByIdOrThrow(id));
        model.addAttribute("registrations", registrationService.getEventRegistrations(id));
        model.addAttribute("user", securityUtils.getCurrentUserOrThrow());
        return "manage/registrations";
    }

}
