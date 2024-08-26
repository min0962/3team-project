package com.fridgeraid.fridgeraid.Repository;

import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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




}
