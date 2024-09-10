package com.fridgeraid.fridgeraid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fridgeraid.fridgeraid.Repository.ConsumptionRecordRepository;
import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.fridgeraid.fridgeraid.domain.ConsumptionType.CONSUMED;
import static com.fridgeraid.fridgeraid.domain.ConsumptionType.DISCARDED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsumptionRecordService {

    @Value("${openai.api.key}")
    private String key;

    private final ConsumptionRecordRepository consumptionRecordRepository;

    // 저장
    @Transactional
    public void saveConsumptionRecord(ConsumptionRecord consumptionRecord) {
        consumptionRecordRepository.save(consumptionRecord);
    }

    // 월단위 조회
    public List<ConsumptionRecord> findConsumptionRecordsByMonth(String deviceId, int year, int month) {
        return consumptionRecordRepository.findCrByMonth(deviceId, year, month);
    }

    // 월별 및 소비 유형별 조회 (이름, 수량, 가격)
    public List<Object[]> getMonthlyConsumptionByType(String deviceId, int year, int month, ConsumptionType consumptionType) {
        return consumptionRecordRepository.findCrByMonthAndType(deviceId, year, month, consumptionType);
    }

    // 월별 소비 및 지출 분석
    public String analyzeMonthlyConsumption(String deviceId, int year, int month) {
        List<Object[]> records = getMonthlyConsumptionByType(deviceId, year, month, CONSUMED);
        String prompt = generateConsumptionPrompt(records, "월별 소비 및 지출 분석");
        return getChatGptResponse(prompt);
    }

    // 월별 폐기물 분석
    public String analyzeMonthlyWaste(String deviceId, int year, int month) {
        List<Object[]> records = getMonthlyConsumptionByType(deviceId, year, month, DISCARDED);
        String prompt = generateWastePrompt(records, "월별 폐기물 분석");
        return getChatGptResponse(prompt);
    }

    // GPT 프롬프트 생성 (소비 및 지출 분석용)
    private String generateConsumptionPrompt(List<Object[]> records, String analysisType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(analysisType).append("을 위해 주어진 데이터를 바탕으로 분석을 수행해 주세요. 결과는 한국어로 제공해 주세요. 분석 결과는 하나의 문자열로 반환되며, 각 문장은 줄바꿈 처리되어야 합니다. 데이터를 나열하는 것보다 인사이트를 제공하는 방식으로 작성해 주세요.\n");
        prompt.append("다음은 데이터입니다:\n");
        for (Object[] record : records) {
            prompt.append("이름: ").append(record[0])
                    .append(", 수량: ").append(record[1])
                    .append(", 단가: ").append(record[2])
                    .append(", 총 가격: ").append(record[3])
                    .append("\n");
        }
        return prompt.toString();
    }

    // GPT 프롬프트 생성 (폐기물 분석용)
    private String generateWastePrompt(List<Object[]> records, String analysisType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(analysisType).append("을 위해 주어진 데이터를 바탕으로 분석을 수행해 주세요. 결과는 한국어로 제공해 주세요. 분석 결과는 하나의 문자열로 반환되며, 각 문장은 줄바꿈 처리되어야 합니다. 데이터를 나열하는 것보다 인사이트를 제공하는 방식으로 작성해 주세요.\n");
        prompt.append("다음은 데이터입니다:\n");
        for (Object[] record : records) {
            prompt.append("이름: ").append(record[0])
                    .append(", 수량: ").append(record[1])
                    .append(", 단가: ").append(record[2])
                    .append(", 총 가격: ").append(record[3])
                    .append("\n");
        }
        return prompt.toString();
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
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ArrayList<Message> list = new ArrayList<>();
        list.add(new Message("user", message));

        Body body = new Body("gpt-3.5-turbo", list);

        RequestEntity<Body> requestEntity = new RequestEntity<>(body, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        return parseStringResponse(responseEntity.getBody());
    }

    // JSON 형식 응답을 단순 문자열로 처리하는 메소드
    private String parseStringResponse(String responseBody) {
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
        return "Error parsing response";
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
