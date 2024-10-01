package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService CrawlingService;

    // 1. 제목과 링크를 10개 가져오는 API
    @GetMapping("/top")
    public ResponseEntity<List<Map<String, String>>> getTop10Recipes() {
        List<Map<String, String>> recipes = CrawlingService.getRecipeTitlesAndLinks();
        return ResponseEntity.ok(recipes);
    }

    // 2. 링크를 받아서 재료와 조리 순서를 가져오는 API
    @GetMapping("/details")
    public ResponseEntity<String> getRecipeDetails(@RequestParam("link") String link) {
        String recipeDetails = CrawlingService.getIngredientsAndSteps(link);
        return ResponseEntity.ok(recipeDetails);
    }
}
