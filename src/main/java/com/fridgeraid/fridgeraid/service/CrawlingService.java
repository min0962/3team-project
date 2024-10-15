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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CrawlingService {

    // 1. 제목과 링크를 10개 추출하는 메소드
    public List<Map<String, String>> getRecipeTitlesAndLinks() {
        List<Map<String, String>> recipeList = new ArrayList<>();

        try {
            // 웹 페이지에서 HTML 문서 가져오기
            Document doc = Jsoup.connect("https://www.10000recipe.com/ranking/home_new.html?dtype=m&rtype=r").get();

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

            // HTML 구조 내에서 재료와 양념 추출
            String ingredientsSection = " " + parseIngredientsFromHTML(doc);
            String stepsSection = parseSteps(doc);

            // 재료와 조리 순서 합치기
            return ingredientsSection + "\n\n" + stepsSection;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseIngredientsFromHTML(Document doc) {
        StringBuilder ingredientsBuilder = new StringBuilder();

        // 재료와 양념이 포함된 <div> 영역 선택
        Element ingredientSection = doc.selectFirst("div#divConfirmedMaterialArea");

        if (ingredientSection != null) {
            // <ul> 태그 내에서 재료/양념 리스트를 탐색
            Elements ulElements = ingredientSection.select("ul");

            for (Element ul : ulElements) {
                // <b> 태그로 섹션 제목을 구분
                String sectionTitle = ul.selectFirst("b").text(); // 예: [재료], [양념장]

                // <li> 태그 내 재료명과 그 양을 추출
                Elements liElements = ul.select("li");

                if (sectionTitle.contains("재료")) {
                    // [재료] 섹션에서는 제목을 출력하지 않고 재료만 출력
                    for (Element li : liElements) {
                        String ingredientName = li.selectFirst("div.ingre_list_name a").text();
                        String ingredientAmount = li.selectFirst("span.ingre_list_ea").text();
                        ingredientsBuilder.append(ingredientName).append(" - ").append(ingredientAmount).append("\n");
                    }
                } else {
                    // [양념] 섹션은 제목과 함께 출력
                    ingredientsBuilder.append(sectionTitle).append(":\n");
                    for (Element li : liElements) {
                        String ingredientName = li.selectFirst("div.ingre_list_name a").text();
                        String ingredientAmount = li.selectFirst("span.ingre_list_ea").text();
                        ingredientsBuilder.append(ingredientName).append(" - ").append(ingredientAmount).append("\n");
                    }
                }
            }
        } else {
            return "재료 정보 없음";
        }

        return ingredientsBuilder.toString().trim();
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
