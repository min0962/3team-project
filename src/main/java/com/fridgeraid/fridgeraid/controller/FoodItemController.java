package com.fridgeraid.fridgeraid.controller;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import com.fridgeraid.fridgeraid.service.FoodeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
}
