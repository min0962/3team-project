package com.fridgeraid.fridgeraid.service;

import com.fridgeraid.fridgeraid.Repository.ConsumptionRecordRepository;
import com.fridgeraid.fridgeraid.Repository.FoodItemRepository;
import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FoodeItemService {
    private final FoodItemRepository foodItemRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    // 식품 전체 조회
    public List<FoodItem> findAllFoodItems(String deviceId) {
        return foodItemRepository.findFoodItemsByDeviceId(deviceId);
    }

    // 보관 방법 별 조회
    public List<FoodItem> findFoodItemsByStorageMethod(String deviceId, StorageMethod storageMethod) {
        return foodItemRepository.findByDeviceNameAndStorageMethod(deviceId, storageMethod);
    }

    // 식품 추가
    @Transactional
    public Integer addFoodItem(FoodItem foodItem) {
        foodItemRepository.save(foodItem);
        return foodItem.getFoodId();
    }

    // 식품 삭제
    @Transactional
    public void deleteFoodItem(Integer foodItemId) {
        foodItemRepository.deleteByFoodId(foodItemId);
    }

    // 식품 수량 업데이트 + 소비 DB에 추가
    @Transactional
    public void updateFoodItemQuantity(Integer foodItemId, BigDecimal quantityToUpdate, ConsumptionType consumptionType) {
        FoodItem foodItem = foodItemRepository.findByFoodId(foodItemId);
        if (foodItem != null) {
            BigDecimal updatedQuantity = foodItem.getQuantity().subtract(quantityToUpdate);
            if (updatedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("수량이 부족합니다.");
            }
            foodItem.setQuantity(updatedQuantity);
            foodItemRepository.save(foodItem);

            // 소비 기록 추가
            ConsumptionRecord consumptionRecord = new ConsumptionRecord();
            consumptionRecord.setDeviceId(foodItem.getDeviceId());
            consumptionRecord.setFoodName(foodItem.getFoodName());
            consumptionRecord.setPrice(foodItem.getPrice());
            consumptionRecord.setQuantity(quantityToUpdate);
            consumptionRecord.setConsumptionDate(LocalDateTime.now());
            consumptionRecord.setConsumptionType(consumptionType);

            consumptionRecordRepository.save(consumptionRecord);
        } else {
            throw new IllegalArgumentException("해당 ID의 식품이 존재하지 않습니다.");
        }
    }
}
