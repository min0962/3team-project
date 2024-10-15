package com.fridgeraid.fridgeraid.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class GptTest {

    public String askGpt(String question, String apiKey) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("service", "gpt");
            jsonParam.put("question", question);
            jsonParam.put("hash", apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://cesrv.hknu.ac.kr/srv/gpt"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonParam.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 유니코드 문자열 디코딩 처리
            String decodedResponse = decodeUnicode(response.body());

            System.out.println("Response Code : " + response.statusCode());
            // 디코딩된 응답을 반환
            return decodedResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    // 유니코드 문자열 디코딩 메소드
    public static String decodeUnicode(String unicode) {
        StringBuilder str = new StringBuilder();
        char[] arr = unicode.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '\\' && arr[i + 1] == 'u') {
                String hex = unicode.substring(i + 2, i + 6);
                str.append((char) Integer.parseInt(hex, 16));
                i += 5;
            } else {
                str.append(arr[i]);
            }
        }
        return str.toString();
    }
}
