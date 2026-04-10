package com.eventflow.dto;

import com.eventflow.model.Event;
import com.eventflow.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Collection of DTOs used throughout EventFlow.
 * Using inner classes for cohesion.
 */
public class EventFlowDtos {

    // ─── Event DTOs ───────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class EventCreateDto {
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 200)
        private String title;

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 5000)
        private String description;

        @NotNull(message = "Date is required")
        @FutureOrPresent(message = "Event date must be today or in the future")
        private LocalDate eventDate;

        @NotNull(message = "Time is required")
        private LocalTime eventTime;

        @NotBlank(message = "Location is required")
        private String location;

        @Min(value = 1, message = "Must allow at least 1 participant")
        @Max(value = 100000)
        private int maxParticipants;

        private Event.Category category = Event.Category.OTHER;

        @DecimalMin("0.0")
        private double price = 0.0;

        private String imageUrl;
        private String additionalInfo;
        private boolean requiresApproval = false;
    }

    @Data
    @NoArgsConstructor
    public static class EventSummaryDto {
        private Long id;
        private String title;
        private String description;
        private LocalDate eventDate;
        private LocalTime eventTime;
        private String location;
        private int maxParticipants;
        private int registeredCount;
        private int availableSpots;
        private Event.Category category;
        private Event.Status status;
        private String imageUrl;
        private double price;
        private boolean isFull;
        private boolean isPast;
        private String createdByName;

        public static EventSummaryDto from(Event event) {
            EventSummaryDto dto = new EventSummaryDto();
            dto.id = event.getId();
            dto.title = event.getTitle();
            dto.description = event.getDescription();
            dto.eventDate = event.getEventDate();
            dto.eventTime = event.getEventTime();
            dto.location = event.getLocation();
            dto.maxParticipants = event.getMaxParticipants();
            dto.registeredCount = event.getRegisteredCount();
            dto.availableSpots = event.getAvailableSpots();
            dto.category = event.getCategory();
            dto.status = event.getStatus();
            dto.imageUrl = event.getImageUrl();
            dto.price = event.getPrice();
            dto.isFull = event.isFull();
            dto.isPast = event.isPast();
            if (event.getCreatedBy() != null) {
                dto.createdByName = event.getCreatedBy().getFullName();
            }
            return dto;
        }
    }

    // ─── User DTOs ────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class UserRegistrationDto {
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "Please confirm your password")
        private String confirmPassword;

        public boolean passwordsMatch() {
            return password != null && password.equals(confirmPassword);
        }
    }

    @Data
    @NoArgsConstructor
    public static class UserProfileDto {
        @NotBlank
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank
        @Size(min = 2, max = 50)
        private String lastName;

        @Size(max = 500)
        private String bio;
    }

    @Data
    @NoArgsConstructor
    public static class UserSummaryDto {
        private Long id;
        private String fullName;
        private String email;
        private User.Role role;
        private boolean active;
        private long eventCount;
        private String createdAt;

        public static UserSummaryDto from(User user) {
            UserSummaryDto dto = new UserSummaryDto();
            dto.id = user.getId();
            dto.fullName = user.getFullName();
            dto.email = user.getEmail();
            dto.role = user.getRole();
            dto.active = user.isActive();
            dto.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "";
            return dto;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreateDataAdderDto {
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8)
        private String password;
    }

    // ─── AI DTOs ──────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class AiChatRequestDto {
        @NotBlank
        private String message;
    }

    @Data
    @NoArgsConstructor
    public static class AiChatResponseDto {
        private String action;
        private String message;
        private Object data;
        private boolean success;
        private String errorMessage;

        public static AiChatResponseDto success(String action, String message, Object data) {
            AiChatResponseDto dto = new AiChatResponseDto();
            dto.action = action;
            dto.message = message;
            dto.data = data;
            dto.success = true;
            return dto;
        }

        public static AiChatResponseDto error(String errorMessage) {
            AiChatResponseDto dto = new AiChatResponseDto();
            dto.success = false;
            dto.errorMessage = errorMessage;
            dto.action = "ERROR";
            return dto;
        }
    }

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    public static class DashboardStatsDto {
        private long totalEvents;
        private long upcomingEvents;
        private long totalUsers;
        private long totalDataAdders;
        private long totalRegistrations;
        private long activeEvents;
    }
}
