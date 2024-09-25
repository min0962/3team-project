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

    // 단일 식품 추가
    @Transactional
    public Integer addFoodItem(FoodItem foodItem) {
        foodItemRepository.save(foodItem);
        return foodItem.getFoodId();
    }

    // 여러 개의 식품 추가
    @Transactional
    public void addFoodItems(List<FoodItem> foodItems) {
        for (FoodItem foodItem : foodItems) {
            foodItemRepository.save(foodItem);
        }
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

            // 수량이 0이면 식품 삭제
            if (updatedQuantity.compareTo(BigDecimal.ZERO) == 0) {
                foodItemRepository.deleteByFoodId(foodItemId);
            } else {
                foodItem.setQuantity(updatedQuantity);
                foodItemRepository.save(foodItem);
            }

            // 소비 기록 업데이트
            List<ConsumptionRecord> existingRecords = consumptionRecordRepository.findCrByDeviceId(foodItem.getDeviceId());

            boolean recordFound = false;
            for (ConsumptionRecord record : existingRecords) {
                if (record.getFoodId().equals(foodItemId)) {
                    // 기존 기록이 있다면 수량 업데이트
                    record.setQuantity(record.getQuantity().add(quantityToUpdate));
                    consumptionRecordRepository.save(record);
                    recordFound = true;
                    break;
                }
            }

            // 기존 기록이 없으면 새 기록 추가
            if (!recordFound) {
                ConsumptionRecord newRecord = new ConsumptionRecord();
                newRecord.setDeviceId(foodItem.getDeviceId());
                newRecord.setFoodName(foodItem.getFoodName());
                newRecord.setPrice(foodItem.getPrice());
                newRecord.setQuantity(quantityToUpdate);
                newRecord.setConsumptionDate(LocalDateTime.now());
                newRecord.setConsumptionType(consumptionType);
                newRecord.setFoodId(foodItemId); // 새로 추가된 필드

                consumptionRecordRepository.save(newRecord);
            }
        } else {
            throw new IllegalArgumentException("해당 ID의 식품이 존재하지 않습니다.");
        }
    }
}
