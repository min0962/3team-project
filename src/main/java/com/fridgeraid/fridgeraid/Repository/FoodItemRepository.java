package com.fridgeraid.fridgeraid.Repository;

import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FoodItemRepository {

    private final EntityManager em;

    //저장
    public void save(FoodItem foodItem) { em.persist(foodItem);}

    //전체 조회
    public List<FoodItem> findFoodItemsByDeviceId(String deviceId) {
        return em.createQuery("select f from FoodItem f where f.deviceId = :deviceId", FoodItem.class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    //foodId 조회
    public FoodItem findByFoodId(Integer foodId) {
        return em.find(FoodItem.class, foodId);
    }

    // 이름과 수량 조회
    public List<Object[]> findNameAndQuantityByDeviceId(String deviceId) {
        return em.createQuery("select f.foodName, f.quantity from FoodItem f where f.deviceId = :deviceId", Object[].class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    // Id로 정보 반환
    public Object[] findFoodDetailsByFoodId(Integer foodId) {
        return em.createQuery("SELECT f.foodId, f.foodName, f.price, f.quantity, f.expirationDate FROM FoodItem f WHERE f.foodId = :foodId", Object[].class)
                .setParameter("foodId", foodId)
                .getSingleResult();  // 단일 결과 반환
    }

    // 현재 날짜와 정수형 day를 더한 범위 내에 있는 식품 조회
    public List<Object[]> findFoodItemsExpiringWithinDays(String deviceId, int days) {
        LocalDate currentDate = LocalDate.now(); // 현재 날짜
        LocalDate targetDate = currentDate.plusDays(days); // 현재 날짜 + days

        return em.createQuery("SELECT f.foodId, f.expirationDate, f.foodName, f.quantity " +
                        "FROM FoodItem f " +
                        "WHERE f.deviceId = :deviceId AND f.expirationDate BETWEEN :currentDate AND :targetDate", Object[].class)
                .setParameter("deviceId", deviceId)
                .setParameter("currentDate", currentDate)
                .setParameter("targetDate", targetDate)
                .getResultList();
    }



    //삭제
    public void deleteByFoodId(Integer foodId) {
        FoodItem foodItem = em.find(FoodItem.class, foodId);
        if (foodItem != null) {
            em.remove(foodItem);
        }
    }

    // 보관장소 별 조회
    public List<FoodItem> findByDeviceNameAndStorageMethod(String deviceId, StorageMethod storageMethod) {
        return em.createQuery("select f from FoodItem f where f.deviceId = :deviceName and f.storageMethod = :storageMethod", FoodItem.class)
                .setParameter("deviceName", deviceId)
                .setParameter("storageMethod", storageMethod)
                .getResultList();
    }

    // 이름과 deviceId로 식품 찾기
    public List<FoodItem> findByFoodNameAndDeviceId(String foodName, String deviceId) {
        return em.createQuery("SELECT f FROM FoodItem f WHERE f.foodName = :foodName AND f.deviceId = :deviceId", FoodItem.class)
                .setParameter("foodName", foodName)
                .setParameter("deviceId", deviceId)
                .getResultList();  // 여러 개의 결과를 받아올 수 있도록 변경
    }



}
