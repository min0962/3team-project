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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

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

    // GPT 요청 메소드 (temperature 추가)
    private String getChatGptResponse(String message, Double temperature) {
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

        // temperature를 요청 본문에 포함 (null이 올 수 있으므로 조건부 처리)
        Body body = new Body("gpt-3.5-turbo", list, temperature);

        RequestEntity<Body> httpEntity = new RequestEntity<>(body, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<String> exchange = restTemplate.exchange(httpEntity, String.class);

        // 응답 내용 로그 출력
        System.out.println("OpenAI API Response: " + exchange.getBody());

        return exchange.getBody();
    }


    // 레시피 이름으로 레시피 정보 반환 (변경 없음)
    public String getRecipeByName(String recipeName) {
        String prompt = "Please provide the recipe for " + recipeName + " in Korean in the following JSON format: " +
                "{\"요리\": \"\", \"재료\": \"재료1 양, 재료2 양, ...\", \"조리순서\": \"1. 설명\\n2. 설명\\n3. 설명\\n...\"}. " +
                "Make sure that each step in '조리순서' is separated by a '\\n' character to ensure proper line breaks in the string format.";
        return parseJsonResponse(getChatGptResponse(prompt, null));  // temperature는 null로 유지
    }

    // 인기 레시피 정보 반환 (temperature 적용)
    public String getPopularRecipes() {
        String prompt = "Please list the top 50 popular homemade Korean dishes. Then, randomly select 3 dishes from the list and return only their names in Korean in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the selection is random every time this prompt is run, and do not return the same 3 dishes on consecutive runs.";
        return parseJsonResponse(getChatGptResponse(prompt, 1.3));
    }

    // 오늘의 레시피 정보 반환 (temperature 적용)
    public String getTodayRecipe() {
        String prompt = "Please provide 3 unique and diverse Korean recipes for today in the following JSON format: [\"요리1\", \"요리2\", \"요리3\"]. Ensure the response strictly follows this format and includes exactly 3 different recipes.";
        return parseJsonResponse(getChatGptResponse(prompt, 1.3));
    }

    // 냉장고 속 재료로 메뉴 추천
    public String getRecommendationByFridgeItems(String deviceId) {
        List<Object[]> items = foodItemRepository.findNameAndQuantityByDeviceId(deviceId);
        StringBuilder prompt = new StringBuilder("내 냉장고에 있는 재료로 만들 수 있는 요리 3개를 추천해주세요. ");
        prompt.append("재료는 다음과 같습니다: ");
        for (Object[] item : items) {
            prompt.append(item[0]).append(" (").append(item[1]).append("), ");
        }
        prompt.append("위 재료들만을 사용하고, 정확히 3개의 요리 이름만 JSON 형식으로 [\"요리1\", \"요리2\", \"요리3\"]로 출력해주세요. 다른 설명은 필요 없습니다.");

        return parseJsonResponse(getChatGptResponse(prompt.toString(), 0.0));
    }



    // 재료 손질 방법 반환 (변경 없음)
    public String getHandlingMethod(String ingredient) {
        String prompt = "Please provide the handling method for " + ingredient + " in Korean with numbered steps, e.g., \"1. 설명\", \"2. 설명\". The response should include as many steps as necessary but ensure that each step is meaningful and avoid repetition.";
        return parseTextResponse(getChatGptResponse(prompt, null));  // temperature는 null로 유지
    }

    // 재료 보관 방법 반환 (변경 없음)
    public String getStorageMethod(String ingredient) {
        String prompt = "Please provide the storage method for " + ingredient + " in Korean with numbered steps, e.g., \"1. 설명\", \"2. 설명\". The response should include as many steps as necessary but ensure that each step is meaningful and avoid repetition.";
        return parseTextResponse(getChatGptResponse(prompt, null));  // temperature는 null로 유지
    }

    // 레시피 변형 요청 메소드
    public String modifyRecipeWithFridgeItems(String deviceId, String recipe, String menu) {
        // 냉장고 속 재료 가져오기
        List<Object[]> fridgeItems = foodItemRepository.findNameAndQuantityByDeviceId(deviceId);
        StringBuilder fridgeIngredients = new StringBuilder();

        // 냉장고에 있는 재료를 StringBuilder로 구성
        for (Object[] item : fridgeItems) {
            fridgeIngredients.append(item[0]).append(" (").append(item[1]).append("), ");
        }

        // GPT에게 보낼 프롬프트 생성
        String prompt = "The menu is \"" + menu + "\", and its recipe is as follows: \"" + recipe +
                "\". Please modify this recipe so that it can be made using only the ingredients from my fridge. " +
                "Here are the ingredients I have: " + fridgeIngredients.toString() +
                ". Adjust the recipe accordingly using only these ingredients. Provide the instructions and ingredients list **in Korean**. " +
                "Ensure to include only the necessary ingredients and exclude others.";

        // GPT에게 요청
        return parseTextResponse(getChatGptResponse(prompt, 1.0));  // 적절한 temperature 설정
    }

    // 사용한 재료 반환
    public String parseIngredientsToJson(String ingredientsSection) {
        // 각 재료와 양을 추출하는 정규 표현식
        Pattern ingredientPattern = Pattern.compile("-\\s*([^\\d-]+)\\s+([\\d\\.]+)");
        Matcher ingredientMatcher = ingredientPattern.matcher(ingredientsSection);

        // Map을 사용하여 재료와 양을 저장
        Map<String, String> ingredientsMap = new HashMap<>();

        while (ingredientMatcher.find()) {
            String ingredient = ingredientMatcher.group(1).trim();
            String amount = ingredientMatcher.group(2).trim();
            ingredientsMap.put(ingredient, amount);
        }

        // Map을 JSONObject로 변환하여 JSON 형태로 출력
        JSONObject jsonObject = new JSONObject(ingredientsMap);
        return jsonObject.toString(4);  // 들여쓰기 4로 포맷된 JSON 반환
    }


    @AllArgsConstructor
    @Data
    static class Body {
        String model;
        List<Message> messages;
        Double temperature;  // temperature 필드 추가 (기본값 null 허용)

        // 생성자에서 temperature가 null일 경우를 처리 (옵션)
        public Body(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
            this.temperature = null;  // 기본값 null
        }
    }


    @AllArgsConstructor
    @Data
    static class Message {
        String role;
        String content;
    }
}
