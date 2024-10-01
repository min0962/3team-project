package com.fridgeraid.fridgeraid.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CrawlingService {

    // 1. 제목과 링크를 10개 추출하는 메소드
    public List<Map<String, String>> getRecipeTitlesAndLinks() {
        List<Map<String, String>> recipeList = new ArrayList<>();

        try {
            // 웹 페이지에서 HTML 문서 가져오기
            Document doc = Jsoup.connect("https://www.10000recipe.com/ranking/home_new.html").get();

            // 요리 제목과 링크 추출
            Elements recipes = doc.select("ul.common_sp_list_ul.ea4 li.common_sp_list_li");

            for (int i = 0; i < Math.min(10, recipes.size()); i++) {
                Element recipe = recipes.get(i);

                // 제목 추출
                String title = recipe.select("div.common_sp_caption_tit.line2").text();

                // 링크 추출
                String link = "https://www.10000recipe.com" + recipe.select("a.common_sp_link").attr("href");

                // 제목과 링크를 맵에 저장
                Map<String, String> recipeMap = new HashMap<>();
                recipeMap.put("title", title);
                recipeMap.put("link", link);

                // 리스트에 추가
                recipeList.add(recipeMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipeList;
    }

    // 2. 링크를 받아서 재료와 조리 순서를 추출하는 메소드
    public String getIngredientsAndSteps(String recipeLink) {
        try {
            // 웹 페이지에서 HTML 문서 가져오기
            Document doc = Jsoup.connect(recipeLink).get();

            // Meta 태그에서 "description" 속성 추출 (재료 파싱)
            String description = doc.select("meta[name=description]").attr("content");

            // [재료]부터 [조리]까지 텍스트 파싱
            String ingredientsSection = " " + parseIngredients(description, doc);

            // 조리 순서 파싱
            String stepsSection = parseSteps(doc);

            // 재료와 조리 순서 합치기
            return ingredientsSection + "\n\n" + stepsSection;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // [재료] 부분을 파싱하는 메소드

    private String parseIngredients(String description, Document doc) {
        // [재료] 혹은 [재료 ] 패턴을 인식하기 위한 정규식
        Pattern pattern = Pattern.compile("\\[재료\\s*\\]");
        Matcher matcher = pattern.matcher(description);

        if (!matcher.find()) {
            return "재료 정보 없음";
        }

        int startIndex = matcher.end(); // [재료] 이후의 인덱스를 찾음

        // stepDiv1 요소 찾기 (조리 순서가 시작하는 부분)
        Element stepDiv1 = doc.selectFirst("div[id=stepDiv1]");
        int endIndex;

        if (stepDiv1 != null) {
            // stepDiv1 요소가 있을 경우, 그 이전까지의 문자열을 사용
            String stepDiv1Text = stepDiv1.text();
            endIndex = description.indexOf(stepDiv1Text);
        } else {
            // stepDiv1이 없으면 문자열 끝까지 사용
            endIndex = description.length();
        }

        String ingredients = description.substring(startIndex, endIndex).trim();

        // 다른 대괄호 패턴 (예: [양념], [고기 밑간])을 기준으로 줄바꿈 처리
        ingredients = ingredients.replaceAll("\\[(.*?)\\]", "\n[$1]\n");

        // 재료 줄바꿈 처리 (',' 기준으로 줄바꿈)
        return ingredients.replace(",", "\n").trim();
    }



    // 조리 순서를 파싱하는 메소드
    private String parseSteps(Document doc) {
        StringBuilder stepsBuilder = new StringBuilder("");

        // 조리 순서가 들어 있는 stepDiv 요소들 가져오기
        Elements steps = doc.select("div.view_step div[id^=stepDiv]");

        for (Element step : steps) {
            String stepDescription = step.select("div.media-body").text();
            stepsBuilder.append(stepDescription).append("\n");
        }

        return stepsBuilder.toString().trim();
    }
}
