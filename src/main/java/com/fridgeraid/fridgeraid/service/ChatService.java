package com.fridgeraid.fridgeraid.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${school.api.key}")
    private String apiKey;

    public String getChatResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                .fromUriString("https://cesrv.hknu.ac.kr/srv/gpt")
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; utf-8");

        // JSON 형식의 요청 바디 구성
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("service", "gpt");
        jsonBody.put("question", message);
        jsonBody.put("hash", apiKey);

        RequestEntity<String> request = new RequestEntity<>(jsonBody.toString(), headers, HttpMethod.POST, uri);

        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return "Error: " + response.getStatusCode();
        }
    }
}
