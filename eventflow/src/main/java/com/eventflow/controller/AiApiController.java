package com.eventflow.controller;

import com.eventflow.dto.EventFlowDtos;
import com.eventflow.model.User;
import com.eventflow.security.SecurityUtils;
import com.eventflow.service.AiCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage/api")
public class AiApiController {

    private final SecurityUtils securityUtils;
    private final AiCommandService aiCommandService;

    public AiApiController(SecurityUtils securityUtils, AiCommandService aiCommandService) {
        this.securityUtils = securityUtils;
        this.aiCommandService = aiCommandService;
    }

    @PostMapping("/chat")
    public ResponseEntity<EventFlowDtos.AiChatResponseDto> chat(@RequestBody EventFlowDtos.AiChatRequestDto request) {
        // Authenticates user and processes the message
        User user = securityUtils.getCurrentUserOrThrow();

        // handleMessage uses the AiConversation logic to track actions like CREATE or DELETE
        EventFlowDtos.AiChatResponseDto response = aiCommandService.handleMessage(request.getMessage(), user);

        return ResponseEntity.ok(response);
    }
}