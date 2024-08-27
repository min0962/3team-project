package com.fridgeraid.fridgeraid.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisionService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String extractTextFromImageFile(MultipartFile imageFile) throws Exception {
        // 1. Google Vision API를 통해 이미지에서 텍스트 추출
        ByteString imgBytes = ByteString.readFrom(imageFile.getInputStream());

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        String extractedText;
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            StringBuilder stringBuilder = new StringBuilder();
            for (AnnotateImageResponse res : response.getResponsesList()) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                    return "Error detected";
                }
                stringBuilder.append(res.getFullTextAnnotation().getText());
            }
            extractedText = stringBuilder.toString();
        }

        // 2. 추출된 텍스트를 ChatGPT API로 전송하여 JSON 형식으로 변환된 결과 받기
        return getJsonFromChatGpt(extractedText);
    }

    private String getJsonFromChatGpt(String extractedText) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.openai.com/v1/chat/completions")
                .build()
                .encode()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + openAiApiKey);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        String prompt = "The following text is from a supermarket receipt read using OCR. Please correct any OCR errors or misreadings, and extract the items as a JSON array of arrays. Each array should contain the item name, quantity, and unit price in Korean won (₩). The format should be [[\"item1\", quantity1, unit_price1], [\"item2\", quantity2, unit_price2], ...]. The text is: \n" + extractedText;

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));

        Body requestBody = new Body("gpt-3.5-turbo", messages);

        RequestEntity<Body> requestEntity = new RequestEntity<>(requestBody, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        return parseChatGptResponse(responseEntity.getBody());
    }

    private String parseChatGptResponse(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode messageContent = choices.get(0).path("message").path("content");
                String jsonString = messageContent.asText();

                // JSON 문자열에서 이스케이프 문자 제거
                jsonString = jsonString.replace("\\n", "").replace("\\\"", "\"").replace("\\\\", "\\");

                // JSON 문자열을 다시 JSON 객체로 변환하여 보기 좋게 포맷
                JsonNode formattedJson = objectMapper.readTree(jsonString);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formattedJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error parsing response";
    }

    private static class Body {
        @JsonProperty("model")
        String model;

        @JsonProperty("messages")
        List<Message> messages;

        public Body(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    private static class Message {
        @JsonProperty("role")
        String role;

        @JsonProperty("content")
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
