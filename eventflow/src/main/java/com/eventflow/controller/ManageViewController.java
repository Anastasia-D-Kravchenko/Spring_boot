package com.eventflow.controller;

import com.eventflow.model.AiConversation;
import com.eventflow.model.User;
import com.eventflow.security.SecurityUtils;
import com.eventflow.service.AiCommandService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/manage")
public class ManageViewController {

    private final SecurityUtils securityUtils;
    private final AiCommandService aiCommandService;

    public ManageViewController(SecurityUtils securityUtils, AiCommandService aiCommandService) {
        this.securityUtils = securityUtils;
        this.aiCommandService = aiCommandService;
    }

    @GetMapping("/ai")
    public String aiChat(Model model) {
        User user = securityUtils.getCurrentUserOrThrow();

        // Get the history
        List<AiConversation> history = aiCommandService.getHistory(user);

        // Reverse it here in Java
        List<AiConversation> reversedHistory = new ArrayList<>(history);
        Collections.reverse(reversedHistory);

        model.addAttribute("user", user);
        model.addAttribute("history", reversedHistory); // Pass the already reversed list

        return "manage/ai-chat";
    }
}