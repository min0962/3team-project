package com.fridgeraid.fridgeraid.service;

import com.fridgeraid.fridgeraid.Repository.FoodItemRepository;
import com.fridgeraid.fridgeraid.domain.FoodItem;
import com.fridgeraid.fridgeraid.domain.StorageMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class FoodeItemServiceTest {
    @Autowired FoodItemRepository foodItemRepository;
    @Autowired FoodeItemService foodeItemService;

    @Test
    public void 추가() throws Exception {
        FoodItem foodItem = new FoodItem();
        foodItem.setFoodName("aaa");
        foodItem.setQuantity(new BigDecimal(10));
        foodItem.setPrice(10000);
        foodItem.setDeviceId("1");
        foodItem.setExpirationDate(LocalDate.now());
        foodItem.setStorageMethod(StorageMethod.valueOf("ROOM_TEMPERATURE"));

        foodeItemService.addFoodItem(foodItem);

        FoodItem foodItem2 = new FoodItem();
        foodItem2.setFoodName("aaa");
        foodItem2.setQuantity(new BigDecimal(10));
        foodItem2.setPrice(10000);
        foodItem2.setDeviceId("1");
        foodItem2.setExpirationDate(LocalDate.now());
        foodItem2.setStorageMethod(StorageMethod.valueOf("ROOM_TEMPERATURE"));

        foodeItemService.addFoodItem(foodItem2);

        List<FoodItem> foodItems = foodeItemService.findAllFoodItems("1");
        // 검증
        assertNotNull(foodItems);
        assertEquals(2, foodItems.size());
        assertEquals("aaa", foodItems.get(0).getFoodName());
        assertEquals("aaa", foodItems.get(1).getFoodName());

        // 출력
        foodItems.forEach(item -> System.out.println(item.toString()));

        foodeItemService.deleteFoodItem(1);
    }
}
