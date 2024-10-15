package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.service.ChatService;
import com.fridgeraid.fridgeraid.service.GptTest;
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
    private final GptTest gptTest;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody String message) {
        String response = chatService.getChatResponse(message);
        return ResponseEntity.ok(response);
    }

    // 새로운 GPT 요청 메소드 추가
    @PostMapping("/ask-gpt")
    public ResponseEntity<String> askGpt(@RequestParam String question, @RequestParam String apiKey) {
        String response = gptTest.askGpt(question, apiKey);
        return ResponseEntity.ok(response);
    }

}