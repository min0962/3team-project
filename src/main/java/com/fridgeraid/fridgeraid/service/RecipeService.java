package com.fridgeraid.fridgeraid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fridgeraid.fridgeraid.Repository.FoodItemRepository;
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
public class RecipeService {

    @Value("${openai.api.key}")
    private String key;

    private final FoodItemRepository foodItemRepository;

    // JSON 형식 응답을 처리하는 메소드
    private String parseJsonResponse(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode messageContent = choices.get(0).path("message").path("content");
                String jsonString = messageContent.asText();

                // JSON 문자열 로그 출력
                System.out.println("JSON String: " + jsonString);

                // JSON 문자열에서 이스케이프 문자 제거
                jsonString = jsonString.replace("\\n", "").replace("\\\"", "\"").replace("\\\\", "\\");

                // JSON 문자열을 다시 JSON 객체로 변환하여 보기 좋게 포맷
                JsonNode formattedJson = objectMapper.readTree(jsonString);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formattedJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error parsing JSON response";
    }

    // 텍스트 형식 응답을 처리하는 메소드
    private String parseTextResponse(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode messageContent = choices.get(0).path("message").path("content");
                return messageContent.asText().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error parsing text response";
    }

    // GPT 요청 메소드
    private String getChatGptResponse(String message) {
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

        // 응답 내용 로그 출력
        System.out.println("OpenAI API Response: " + exchange.getBody());

        return exchange.getBody();
    }

    // 레시피 이름으로 레시피 정보 반환
    public String getRecipeByName(String recipeName) {
        String prompt = "Please provide the recipe for " + recipeName + " in Korean in the following JSON format: " +
                "{\"요리\": \"\", \"재료\": \"재료1 양, 재료2 양, ...\", \"조리순서\": \"1. 설명\\n2. 설명\\n3. 설명\\n...\"}. " +
                "Make sure that each step in '조리순서' is separated by a '\\n' character to ensure proper line breaks in the string format.";
        return parseJsonResponse(getChatGptResponse(prompt));
    }
/*
// 레시피 이름으로 레시피 정보 반환
    public String getRecipeByName(String recipeName) {
        String prompt = "Please provide the recipe for " + recipeName + " in Korean in the following JSON format: {\"요리\": \"\", \"재료\": [\"재료1 양\", \"재료2 양\", ...], \"조리순서\": [\"\"]}. Ensure the response strictly follows this format and includes the ingredient amounts.";
        return parseJsonResponse(getChatGptResponse(prompt));
    }
 */


    // 인기 레시피 정보 반환
    public String getPopularRecipes() {
        String prompt = "Please provide 3 unique and diverse popular Korean recipes in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the response strictly follows this format and includes exactly 3 different recipes.";
        return parseJsonResponse(getChatGptResponse(prompt));
    }

    // 오늘의 레시피 정보 반환
    public String getTodayRecipe() {
        String prompt = "Please provide 3 unique and diverse Korean recipes for today in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the response strictly follows this format and includes exactly 3 different recipes.";
        return parseJsonResponse(getChatGptResponse(prompt));
    }

    // 냉장고 속 재료로 메뉴 추천
    public String getRecommendationByFridgeItems(String deviceId) {
        List<Object[]> items = foodItemRepository.findNameAndQuantityByDeviceId(deviceId);
        StringBuilder prompt = new StringBuilder("Based on the following ingredients in my fridge, suggest 3 unique and diverse Korean recipes using only these ingredients and common seasonings in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ingredients: ");
        for (Object[] item : items) {
            prompt.append(item[0]).append(" (").append(item[1]).append("), ");
        }
        prompt.append("Ensure the response strictly follows this format and includes exactly 3 different recipes.");
        return parseJsonResponse(getChatGptResponse(prompt.toString()));
    }

    // 재료 손질 방법 반환
    public String getHandlingMethod(String ingredient) {
        String prompt = "Please provide the handling method for " + ingredient + " in Korean with numbered steps, e.g., \"1. 설명\", \"2. 설명\". The response should include as many steps as necessary but ensure that each step is meaningful and avoid repetition.";
        return parseTextResponse(getChatGptResponse(prompt));
    }

    // 재료 보관 방법 반환
    public String getStorageMethod(String ingredient) {
        String prompt = "Please provide the storage method for " + ingredient + " in Korean with numbered steps, e.g., \"1. 설명\", \"2. 설명\". The response should include as many steps as necessary but ensure that each step is meaningful and avoid repetition.";
        return parseTextResponse(getChatGptResponse(prompt));
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
