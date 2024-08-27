package com.fridgeraid.fridgeraid.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${openai.api.key}")
    private String key;

    public String getChatResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.openai.com/v1/chat/completions")
                .build()
                .encode()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + key);

        ArrayList<Message> list = new ArrayList<>();
        list.add(new Message("user", message));

        Body body = new Body("gpt-3.5-turbo", list);

        RequestEntity<Body> httpEntity = new RequestEntity<>(body, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<String> exchange = restTemplate.exchange(httpEntity, String.class);
        return exchange.getBody();
    }

    @AllArgsConstructor
    @Data
    static class Body {
        String model;
        List<Message> messages;
    }

    @AllArgsConstructor
    @Data
    static class Message {
        String role;
        String content;
    }
}