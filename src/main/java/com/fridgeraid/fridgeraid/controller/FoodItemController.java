package com.fridgeraid.fridgeraid.controller;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import com.fridgeraid.fridgeraid.service.FoodeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fooditems")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodeItemService foodeItemService;

    // 식품 전체 조회
    @GetMapping("/{deviceId}")
    public List<FoodItem> getAllFoodItems(@PathVariable String deviceId) {
        return foodeItemService.findAllFoodItems(deviceId);
    }

    // 보관 방법 별 조회
    @GetMapping("/{deviceId}/storage/{storageMethod}")
    public List<FoodItem> getFoodItemsByStorageMethod(
            @PathVariable String deviceId,
            @PathVariable StorageMethod storageMethod) {
        return foodeItemService.findFoodItemsByStorageMethod(deviceId, storageMethod);
    }

    // id로 정보 반환
    @GetMapping("/details/{foodId}")
    public ResponseEntity<Object[]> getFoodDetailsByFoodId(@PathVariable Integer foodId) {
        Object[] foodDetails = foodeItemService.findFoodDetailsByFoodId(foodId);
        if (foodDetails != null) {
            return ResponseEntity.ok(foodDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    // 단일 식품 추가
    @PostMapping
    public Integer addFoodItem(@RequestBody FoodItem foodItem) {
        return foodeItemService.addFoodItem(foodItem);
    }

    // 여러 개의 식품 추가
    @PostMapping("/list")
    public void addFoodItems(@RequestBody List<FoodItem> foodItems) {
        foodeItemService.addFoodItems(foodItems);
    }

    // 식품 삭제
    @DeleteMapping("/delete")
    public void deleteFoodItem(@RequestParam Integer foodItemId) {
        foodeItemService.deleteFoodItem(foodItemId);
    }

    // 식품 수량 업데이트 + 소비 DB에 추가
    @PutMapping("/quantity")
    public void updateFoodItemQuantity(
            @RequestParam Integer foodItemId,
            @RequestParam BigDecimal quantityToUpdate,
            @RequestParam ConsumptionType consumptionType) {
        foodeItemService.updateFoodItemQuantity(foodItemId, quantityToUpdate, consumptionType);
    }

    // 재료 수량 업데이트 및 소비 기록 추가
    @PostMapping("/consume-ingredients")
    public ResponseEntity<String> consumeIngredients(@RequestParam String deviceId, @RequestBody Map<String, BigDecimal> ingredients) {
        try {
            foodeItemService.updateFoodItemsQuantityAndLogConsumption(deviceId, ingredients);
            return ResponseEntity.ok("재료가 성공적으로 소비되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 현재 날짜로부터 주어진 일(days) 내에 유통기한이 만료되는 식품 조회
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Object[]>> getExpiringFoodItems(
            @RequestParam String deviceId,
            @RequestParam int days) {
        List<Object[]> foodItems = foodeItemService.findFoodItemsExpiringWithinDays(deviceId, days);
        return ResponseEntity.ok(foodItems);
    }


}
