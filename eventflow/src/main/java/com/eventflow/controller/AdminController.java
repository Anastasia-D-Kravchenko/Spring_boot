package com.eventflow.controller;

import com.eventflow.dto.EventFlowDtos.*;
import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.security.SecurityUtils;
import com.eventflow.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final SecurityUtils securityUtils;

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalEvents(eventService.countTotal());
        stats.setUpcomingEvents(eventService.countUpcoming());
        stats.setTotalUsers(userService.countByRole(User.Role.USER));
        stats.setTotalDataAdders(userService.countByRole(User.Role.DATA_ADDER));
        stats.setTotalRegistrations(registrationService.countTotalRegistrations());
        stats.setActiveEvents(eventService.countByStatus(Event.Status.ACTIVE));

        model.addAttribute("stats", stats);
        model.addAttribute("recentEvents", eventService.findUpcoming(0, 5).getContent());
        model.addAttribute("categoryStats", eventService.getCategoryStats());
        model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
        return "admin/dashboard";
    }

    // ─── User Management ──────────────────────────────────────────────────────

    @GetMapping("/users")
    public String userList(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "") String q,
                           Model model) {
        Page<User> users = q.isBlank()
                ? userService.findAllUsers(page, 15)
                : userService.searchUsers(q, page, 15);

        model.addAttribute("users", users);
        model.addAttribute("searchQuery", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
        return "admin/users";
    }

    @PostMapping("/users/{id}/promote")
    public String promoteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User admin = securityUtils.getCurrentUserOrThrow();
            userService.promoteToDataAdder(id, admin);
            ra.addFlashAttribute("success", "User promoted to Data Adder successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/demote")
    public String demoteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.demoteToUser(id);
            ra.addFlashAttribute("success", "User demoted to regular user.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.toggleActive(id);
            ra.addFlashAttribute("success", "User status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("success", "User deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ─── Data Adder Creation ──────────────────────────────────────────────────

    @GetMapping("/data-adders/new")
    public String newDataAdderForm(Model model) {
        model.addAttribute("dto", new CreateDataAdderDto());
        model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
        return "admin/new-data-adder";
    }

    @PostMapping("/data-adders/new")
    public String createDataAdder(@Valid @ModelAttribute("dto") CreateDataAdderDto dto,
                                   BindingResult result,
                                   RedirectAttributes ra,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
            return "admin/new-data-adder";
        }
        try {
            User admin = securityUtils.getCurrentUserOrThrow();
            User created = userService.createDataAdder(dto, admin);
            ra.addFlashAttribute("success",
                    "Data Adder '" + created.getFullName() + "' created successfully. " +
                    "Login: " + created.getEmail());
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
            return "admin/new-data-adder";
        }
    }

    // ─── Event Management (admin view) ────────────────────────────────────────

    @GetMapping("/events")
    public String adminEvents(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("events", eventService.findUpcoming(page, 15));
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUser", securityUtils.getCurrentUserOrThrow());
        return "admin/events";
    }

    @PostMapping("/events/{id}/cancel")
    public String cancelEvent(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User admin = securityUtils.getCurrentUserOrThrow();
            eventService.updateStatus(id, Event.Status.CANCELLED, admin);
            ra.addFlashAttribute("success", "Event cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User admin = securityUtils.getCurrentUserOrThrow();
            eventService.deleteEvent(id, admin);
            ra.addFlashAttribute("success", "Event deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events";
    }
}
