package com.fridgeraid.fridgeraid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fridgeraid.fridgeraid.Repository.FoodItemRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
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
public class RecipeService {

    @Value("${school.api.key}")
    private String apiKey;

    private final FoodItemRepository foodItemRepository;

    public String getRecipeByName(String recipeName) {
        String prompt = "Please provide the recipe for " + recipeName + " in Korean in the following JSON format: {\"요리\": \"\", \"재료\": [\"재료1 양\", \"재료2 양\", ...], \"조리순서\": [\"\"]}. Ensure the response strictly follows this format and includes the ingredient amounts.";
        return parseChatGptResponse(getChatGptResponse(prompt));
    }

    public String getPopularRecipes() {
        String prompt = "Please provide 3 unique and diverse popular Korean recipes in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the response strictly follows this format and includes exactly 3 different recipes.";
        return parseChatGptResponse(getChatGptResponse(prompt));
    }

    public String getTodayRecipe() {
        String prompt = "Please provide 3 unique and diverse Korean recipes for today in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the response strictly follows this format and includes exactly 3 different recipes.";
        return parseChatGptResponse(getChatGptResponse(prompt));
    }

    public String getRecommendationByFridgeItems(String deviceId) {
        List<Object[]> items = foodItemRepository.findNameAndQuantityByDeviceId(deviceId);
        StringBuilder prompt = new StringBuilder("Based on the following ingredients in my fridge, suggest 3 unique and diverse Korean recipes using only these ingredients and common seasonings in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ingredients: ");
        for (Object[] item : items) {
            prompt.append(item[0]).append(" (").append(item[1]).append("), ");
        }
        prompt.append("Ensure the response strictly follows this format and includes exactly 3 different recipes.");
        return parseChatGptResponse(getChatGptResponse(prompt.toString()));
    }

    private String getChatGptResponse(String message) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                .fromUriString("https://cesrv.hknu.ac.kr/srv/gpt")
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON 형식의 요청 바디 구성
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("service", "gpt");
        jsonBody.put("question", message);
        jsonBody.put("hash", apiKey);

        RequestEntity<String> request = new RequestEntity<>(jsonBody.toString(), headers, HttpMethod.POST, uri);

        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        // 응답 내용 로그 출력
        System.out.println("School API Response: " + response.getBody());

        return response.getBody();
    }

    private String parseChatGptResponse(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // 응답이 바로 JSON 형식이라면 바로 반환, 아니라면 특정 필드를 찾아야 할 수도 있음
            if (root.isObject() || root.isArray()) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            }

            // JSON 형식이 아니라고 가정하고 기본 문자열을 반환
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error parsing response";
    }
}
