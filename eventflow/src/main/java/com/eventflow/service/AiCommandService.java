package com.eventflow.service;

import com.eventflow.dto.EventFlowDtos.*;
import com.eventflow.model.Event;
import com.eventflow.model.User;
import com.eventflow.repository.AiConversationRepository;
import com.eventflow.model.AiConversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiCommandService {

    private final AnthropicService aiService; // Inject the actual service
    private final EventService eventService;
    private final AiConversationRepository conversationRepository;

    @Transactional
    public AiChatResponseDto handleMessage(String userMessage, User currentUser) {
        String context = buildContext(currentUser);
        Map<String, Object> aiResult = aiService.chat(userMessage, context);

        String action = getString(aiResult, "action", "INFO");
        String message = getString(aiResult, "message", "Processed.");
        Map<String, Object> data = (Map<String, Object>) aiResult.getOrDefault("data", new HashMap<>());

        Object responseData = null;
        boolean success = true;
        String finalMessage = message;

        try {
            // This switch now finds all referenced methods
            responseData = switch (action.toUpperCase()) {
                case "CREATE" -> handleCreate(data, currentUser);
                case "DELETE" -> handleDelete(data, currentUser);
                case "UPDATE" -> handleUpdate(data, currentUser);
                case "LIST"   -> handleList(data);
                case "SEARCH" -> handleSearch(data);
                case "STATS"  -> handleStats();
                default       -> null;
            };
        } catch (Exception e) {
            success = false;
            finalMessage = "❌ " + e.getMessage();
        }

        AiConversation convo = new AiConversation(currentUser, userMessage, finalMessage, action);
        convo.setSuccess(success);
        conversationRepository.save(convo);

        return AiChatResponseDto.success(action, finalMessage, responseData);
    }

    // ─── Restored Action Handlers ───

    private Map<String, Object> handleCreate(Map<String, Object> data, User user) {
        Event event = eventService.createEventFromMap(data, user);
        return Map.of("id", event.getId(), "title", event.getTitle(), "location", event.getLocation());
    }

    private Map<String, Object> handleDelete(Map<String, Object> data, User user) {
        Long id = getLong(data, "id", null);
        if (id == null) throw new IllegalArgumentException("Event ID required for deletion");
        Event event = eventService.findByIdOrThrow(id);
        String title = event.getTitle();
        eventService.deleteEvent(id, user);
        return Map.of("deleted", true, "title", title);
    }

    private Map<String, Object> handleUpdate(Map<String, Object> data, User user) {
        Long id = getLong(data, "id", null);
        if (id == null) throw new IllegalArgumentException("Event ID required for update");
        Event event = eventService.findByIdOrThrow(id);
        if (data.containsKey("title")) event.setTitle(getString(data, "title", event.getTitle()));
        if (data.containsKey("location")) event.setLocation(getString(data, "location", event.getLocation()));
        return Map.of("id", event.getId(), "title", event.getTitle(), "updated", true);
    }

    private List<Map<String, Object>> handleList(Map<String, Object> data) {
        List<Event> events = eventService.findUpcoming(0, 10).getContent();
        return events.stream().map(e -> Map.<String, Object>of("id", e.getId(), "title", e.getTitle(), "location", e.getLocation())).collect(Collectors.toList());
    }

    private List<Map<String, Object>> handleSearch(Map<String, Object> data) {
        String query = getString(data, "query", "");
        return eventService.searchEvents(query, 0, 10).getContent().stream().map(e -> Map.<String, Object>of("id", e.getId(), "title", e.getTitle())).collect(Collectors.toList());
    }

    private Map<String, Object> handleStats() {
        return Map.of("totalEvents", eventService.countTotal(), "upcoming", eventService.countUpcoming());
    }

    private String buildContext(User user) {
        List<Event> myEvents = eventService.findAllActiveByCreator(user);
        StringBuilder sb = new StringBuilder("Today: " + LocalDate.now() + "\nEvents:\n");
        myEvents.forEach(e -> sb.append("- [ID:").append(e.getId()).append("] ").append(e.getTitle()).append("\n"));
        return sb.toString();
    }

    public List<AiConversation> getHistory(User user) {
        return conversationRepository.findTop20ByUserOrderByCreatedAtDesc(user);
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }

    private Long getLong(Map<String, Object> map, String key, Long def) {
        Object val = map.get(key);
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return def; }
    }
}