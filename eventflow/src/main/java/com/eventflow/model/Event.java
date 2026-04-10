package com.eventflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor
@ToString(exclude = {"registrations"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(min = 10, max = 5000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate eventDate;

    @NotNull
    @Column(nullable = false)
    private LocalTime eventTime;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String location;

    @Min(1)
    @Max(100000)
    @Column(nullable = false)
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category = Category.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    private boolean requiresApproval = false;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private double price = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Registration> registrations = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Convenience constructor
    public Event(String title, String description, LocalDate eventDate, LocalTime eventTime,
                 String location, int maxParticipants, Category category, User createdBy) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.category = category;
        this.createdBy = createdBy;
    }

    public int getRegisteredCount() {
        return (int) registrations.stream()
                .filter(r -> r.getStatus() == Registration.RegistrationStatus.CONFIRMED)
                .count();
    }

    public int getAvailableSpots() {
        return maxParticipants - getRegisteredCount();
    }

    public boolean isFull() {
        return getAvailableSpots() <= 0;
    }

    public boolean isPast() {
        return eventDate.isBefore(LocalDate.now());
    }

    public enum Category {
        MUSIC, TECH, SPORTS, ART, FOOD, BUSINESS, WELLNESS, EDUCATION, COMMUNITY, OTHER;

        public String getDisplayName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }

        public String getEmoji() {
            return switch (this) {
                case MUSIC -> "🎵";
                case TECH -> "💻";
                case SPORTS -> "⚽";
                case ART -> "🎨";
                case FOOD -> "🍽️";
                case BUSINESS -> "💼";
                case WELLNESS -> "🧘";
                case EDUCATION -> "📚";
                case COMMUNITY -> "🤝";
                case OTHER -> "✨";
            };
        }
    }

    public enum Status {
        ACTIVE, CANCELLED, COMPLETED, DRAFT
    }
}
