package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    // 요리 이름을 받아 요리 정보 반환
    @PostMapping("/by-name")
    public ResponseEntity<String> getRecipeByName(@RequestParam String name) {
        String response = recipeService.getRecipeByName(name);
        return ResponseEntity.ok(response);
    }

    // 인기 레시피 정보 반환
    @GetMapping("/popular")
    public ResponseEntity<String> getPopularRecipes() {
        String response = recipeService.getPopularRecipes();
        return ResponseEntity.ok(response);
    }

    // 오늘의 레시피 정보 반환
    @GetMapping("/today")
    public ResponseEntity<String> getTodayRecipe() {
        String response = recipeService.getTodayRecipe();
        return ResponseEntity.ok(response);
    }

    // 냉장고 속 재료로 메뉴 추천
    @GetMapping("/recommendation/{deviceId}")
    public ResponseEntity<String> getRecommendationByFridgeItems(@PathVariable String deviceId) {
        String response = recipeService.getRecommendationByFridgeItems(deviceId);
        return ResponseEntity.ok(response);
    }


}
