package com.eventflow.config;

import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventFlowProperties props;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedDataAdder();
        seedSampleEvents();
        log.info("✅ EventFlow data initialization complete");
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@eventflow.com")) {
            User admin = new User(
                    "Admin", "EventFlow",
                    "admin@eventflow.com",
                    passwordEncoder.encode(props.getAdmin().getDefaultPassword()),
                    User.Role.ADMIN
            );
            userRepository.save(admin);
            log.info("✅ Admin created — email: admin@eventflow.com  password: {}", props.getAdmin().getDefaultPassword());
        }
    }

    private void seedDataAdder() {
        if (!userRepository.existsByEmail("manager@eventflow.com")) {
            User dataAdder = new User(
                    "Event", "Manager",
                    "manager@eventflow.com",
                    passwordEncoder.encode("Manager@1234"),
                    User.Role.DATA_ADDER
            );
            userRepository.save(dataAdder);
            log.info("✅ Sample DATA_ADDER created — email: manager@eventflow.com  password: Manager@1234");
        }

        if (!userRepository.existsByEmail("user@eventflow.com")) {
            User regularUser = new User(
                    "John", "Doe",
                    "user@eventflow.com",
                    passwordEncoder.encode("User@1234"),
                    User.Role.USER
            );
            userRepository.save(regularUser);
            log.info("✅ Sample USER created — email: user@eventflow.com  password: User@1234");
        }
    }

    private void seedSampleEvents() {
        if (eventRepository.count() > 0) return;

        User manager = userRepository.findByEmail("manager@eventflow.com").orElseThrow();

        Event e1 = new Event(
                "Spring Boot Deep Dive Workshop",
                "A full-day hands-on workshop covering Spring Boot 3, Spring Data JPA, Spring Security, and building production-ready REST APIs. Perfect for Java developers looking to level up their backend skills.",
                LocalDate.now().plusDays(10), LocalTime.of(9, 0),
                "TechHub Warsaw, ul. Nowy Świat 4", 30, Event.Category.TECH, manager
        );
        e1.setPrice(49.99);

        Event e2 = new Event(
                "Wrocław Jazz Night",
                "An unforgettable evening with top jazz musicians performing live. Enjoy world-class music in a beautiful concert hall in the heart of Wrocław.",
                LocalDate.now().plusDays(5), LocalTime.of(19, 30),
                "Filharmonia Wrocławska, ul. Piłsudskiego 19", 200, Event.Category.MUSIC, manager
        );
        e2.setPrice(25.0);

        Event e3 = new Event(
                "Community Clean-Up Day",
                "Join your neighbors for a city-wide clean-up initiative. We'll meet at Rynek and split into groups. All equipment provided. Free lunch for all volunteers!",
                LocalDate.now().plusDays(3), LocalTime.of(8, 0),
                "Rynek Główny, Wrocław", 100, Event.Category.COMMUNITY, manager
        );

        Event e4 = new Event(
                "AI & Machine Learning Meetup",
                "Monthly meetup for AI enthusiasts. This month's topics: LLM fine-tuning, RAG architectures, and a live demo of building an AI-powered app with Spring Boot + Claude API.",
                LocalDate.now().plusDays(14), LocalTime.of(18, 0),
                "Google Campus, ul. Legnicka 48", 80, Event.Category.TECH, manager
        );

        Event e5 = new Event(
                "Yoga & Mindfulness Retreat",
                "A relaxing weekend retreat combining yoga, meditation, and breathwork. Suitable for all levels. Meals and accommodation included in the ticket price.",
                LocalDate.now().plusDays(21), LocalTime.of(10, 0),
                "Retreat Center Sudety, Szklarska Poręba", 25, Event.Category.WELLNESS, manager
        );
        e5.setPrice(199.0);

        Event e6 = new Event(
                "Startup Founders Brunch",
                "Monthly networking breakfast for founders, investors, and startup enthusiasts. Exchange ideas, find co-founders, and get inspired. Light refreshments included.",
                LocalDate.now().plusDays(7), LocalTime.of(10, 0),
                "Impact Hub Wrocław, ul. Włodkowica 21", 50, Event.Category.BUSINESS, manager
        );
        e6.setPrice(15.0);

        eventRepository.save(e1);
        eventRepository.save(e2);
        eventRepository.save(e3);
        eventRepository.save(e4);
        eventRepository.save(e5);
        eventRepository.save(e6);

        log.info("✅ {} sample events created", 6);
    }
}
