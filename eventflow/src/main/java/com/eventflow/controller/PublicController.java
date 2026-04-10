package com.eventflow.controller;

import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.security.SecurityUtils;
import com.eventflow.service.EventService;
import com.eventflow.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/")
    public String home(Model model) {
        Page<Event> events = eventService.findUpcoming(0, 6);
        model.addAttribute("events", events.getContent());
        model.addAttribute("categories", Event.Category.values());
        model.addAttribute("totalEvents", eventService.countUpcoming());
        addCurrentUser(model);
        return "index";
    }

    @GetMapping("/events")
    public String eventsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String q,
            Model model) {

        Page<Event> events;

        if (!q.isBlank()) {
            events = eventService.searchEvents(q, page, 12);
            model.addAttribute("searchQuery", q);
        } else if (!category.isBlank()) {
            try {
                Event.Category cat = Event.Category.valueOf(category.toUpperCase());
                events = eventService.findByCategory(cat, page, 12);
                model.addAttribute("selectedCategory", cat);
            } catch (Exception e) {
                events = eventService.findUpcoming(page, 12);
            }
        } else {
            events = eventService.findUpcoming(page, 12);
        }

        model.addAttribute("events", events);
        model.addAttribute("categories", Event.Category.values());
        model.addAttribute("currentPage", page);
        addCurrentUser(model);
        return "events/list";
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        Event event = eventService.findByIdOrThrow(id);
        model.addAttribute("event", event);
        model.addAttribute("categories", Event.Category.values());

        Optional<User> currentUser = securityUtils.getCurrentUser();
        currentUser.ifPresent(u -> {
            model.addAttribute("isRegistered", registrationService.isRegistered(u, event));
            model.addAttribute("currentUser", u);
        });
        model.addAttribute("registrationCount", registrationService.countConfirmedForEvent(event));
        return "events/detail";
    }

    @PostMapping("/register-event/{id}")
    public String registerForEvent(@PathVariable Long id) {
        User user = securityUtils.getCurrentUserOrThrow();
        registrationService.registerForEvent(user, id);
        return "redirect:/events/" + id + "?registered=true";
    }

    @PostMapping("/cancel-event/{id}")
    public String cancelRegistration(@PathVariable Long id) {
        User user = securityUtils.getCurrentUserOrThrow();
        registrationService.cancelRegistration(user, id);
        return "redirect:/events/" + id + "?cancelled=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = securityUtils.getCurrentUserOrThrow();
        if (user.getRole() == User.Role.ADMIN) return "redirect:/admin/dashboard";
        if (user.getRole() == User.Role.DATA_ADDER) return "redirect:/manage/dashboard";
        // Regular user dashboard
        model.addAttribute("user", user);
        model.addAttribute("registrations", registrationService.getUserRegistrations(user));
        model.addAttribute("upcomingEvents", eventService.findUpcoming(0, 6).getContent());
        return "user/dashboard";
    }

    private void addCurrentUser(Model model) {
        securityUtils.getCurrentUser().ifPresent(u -> model.addAttribute("currentUser", u));
    }
}
