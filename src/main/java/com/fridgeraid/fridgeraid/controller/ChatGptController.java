package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/chat-gpt")
public class ChatGptController {
    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody String message) {
        String response = chatService.getChatResponse(message);
        return ResponseEntity.ok(response);
    }
}
