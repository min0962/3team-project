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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // id로 정보 반환
    public Object[] findFoodDetailsByFoodId(Integer foodId) {
        return foodItemRepository.findFoodDetailsByFoodId(foodId);
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

    // 단일 재료 업데이트 + 소비 또는 배출 기록 추가
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

            // 소비 또는 배출 기록 업데이트
            List<ConsumptionRecord> existingRecords = consumptionRecordRepository.findCrByDeviceIdAndFoodIdAndConsumptionType(
                    foodItem.getDeviceId(), foodItemId, consumptionType);

            boolean recordFound = false;
            for (ConsumptionRecord record : existingRecords) {
                if (record.getFoodId().equals(foodItemId) && record.getConsumptionType() == consumptionType) {
                    // 기존 기록이 있으면 수량 업데이트
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
                newRecord.setFoodId(foodItemId);

                consumptionRecordRepository.save(newRecord);
            }
        } else {
            throw new IllegalArgumentException("해당 ID의 식품이 존재하지 않습니다.");
        }
    }
    // 중복된 식품을 검색하고 첫 번째 결과를 반환하는 메소드
    public FoodItem findFoodItem(String foodName, String deviceId) {
        List<FoodItem> foodItems = foodItemRepository.findByFoodNameAndDeviceId(foodName, deviceId);

        if (foodItems.isEmpty()) {
            throw new IllegalArgumentException("해당 식품이 존재하지 않습니다.");
        }

        return foodItems.get(0);  // 첫 번째 결과 반환
    }
    // 여러 재료의 수량 업데이트 + 소비 기록 추가
    @Transactional
    public void updateFoodItemsQuantityAndLogConsumption(String deviceId, Map<String, BigDecimal> ingredients) {
        for (Map.Entry<String, BigDecimal> entry : ingredients.entrySet()) {
            String foodName = entry.getKey();
            BigDecimal quantityToUpdate = entry.getValue();

            // 식품 이름으로 해당 재료 찾기
            FoodItem foodItem = findFoodItem(foodName, deviceId);  // 새로 정의한 메소드 사용

            if (foodItem != null) {
                BigDecimal updatedQuantity = foodItem.getQuantity().subtract(quantityToUpdate);

                if (updatedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("수량이 부족합니다: " + foodName);
                }

                // 수량이 0이면 식품 삭제
                if (updatedQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    foodItemRepository.deleteByFoodId(foodItem.getFoodId());
                } else {
                    // 남은 수량 업데이트
                    foodItem.setQuantity(updatedQuantity);
                    foodItemRepository.save(foodItem);
                }

                // 소비 기록 업데이트 (항상 CONSUMED)
                List<ConsumptionRecord> existingRecords = consumptionRecordRepository.findCrByDeviceIdAndFoodIdAndConsumptionType(
                        deviceId, foodItem.getFoodId(), ConsumptionType.CONSUMED);

                boolean recordFound = false;
                for (ConsumptionRecord record : existingRecords) {
                    if (record.getFoodId().equals(foodItem.getFoodId()) && record.getConsumptionType() == ConsumptionType.CONSUMED) {
                        // 기존 기록이 있으면 수량 업데이트
                        record.setQuantity(record.getQuantity().add(quantityToUpdate));
                        consumptionRecordRepository.save(record);
                        recordFound = true;
                        break;
                    }
                }

                // 기존 기록이 없으면 새 기록 추가
                if (!recordFound) {
                    ConsumptionRecord newRecord = new ConsumptionRecord();
                    newRecord.setDeviceId(deviceId);
                    newRecord.setFoodName(foodItem.getFoodName());
                    newRecord.setPrice(foodItem.getPrice());
                    newRecord.setQuantity(quantityToUpdate);
                    newRecord.setConsumptionDate(LocalDateTime.now());
                    newRecord.setConsumptionType(ConsumptionType.CONSUMED);
                    newRecord.setFoodId(foodItem.getFoodId());

                    consumptionRecordRepository.save(newRecord);
                }
            } else {
                throw new IllegalArgumentException("해당 식품이 냉장고에 없습니다: " + foodName);
            }
        }
    }

    // 현재 날짜와 정수형 day를 더한 범위 내에 있는 식품 조회
    public List<Object[]> findFoodItemsExpiringWithinDays(String deviceId, int days) {
        LocalDate currentDate = LocalDate.now(); // 현재 날짜
        LocalDate targetDate = currentDate.plusDays(days); // 현재 날짜 + days

        // 식품을 조회 (식별자, 유통기한, 이름, 수량)
        List<Object[]> foodItems = foodItemRepository.findFoodItemsExpiringWithinDays(deviceId, days);

        // 결과 리스트
        List<Object[]> result = new ArrayList<>();

        // 현재 날짜와의 차이를 계산하여 D-Day 또는 D-몇일 형태로 변환
        for (Object[] foodItem : foodItems) {
            Integer foodId = (Integer) foodItem[0];
            LocalDate expirationDate = (LocalDate) foodItem[1];
            String foodName = (String) foodItem[2];
            BigDecimal quantity = (BigDecimal) foodItem[3];

            // 현재 날짜와의 차이를 계산
            long daysBetween = ChronoUnit.DAYS.between(currentDate, expirationDate);

            // 유통기한을 D-Day 혹은 D-차이로 표시
            String formattedExpiration;
            if (daysBetween == 0) {
                formattedExpiration = "D-Day";
            } else if (daysBetween > 0) {
                formattedExpiration = "D-" + daysBetween;
            } else {
                formattedExpiration = "D+" + Math.abs(daysBetween);
            }

            // 새로운 결과 리스트에 추가
            result.add(new Object[]{foodId, formattedExpiration, foodName, quantity, daysBetween});
        }

        // daysBetween 값을 기준으로 정렬 (작은 값이 먼저 오도록)
        result.sort((a, b) -> Long.compare((long) a[4], (long) b[4]));

        // daysBetween 필드를 제거하고 나머지 필드만 반환
        List<Object[]> finalResult = new ArrayList<>();
        for (Object[] item : result) {
            finalResult.add(new Object[]{item[0], item[1], item[2], item[3]});
        }

        return finalResult;
    }
}
