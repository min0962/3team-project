package com.fridgeraid.fridgeraid.Repository;

import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConsumptionRecordRepository {

    @PersistenceContext
    private final EntityManager em;

    public void save(ConsumptionRecord consumptionRecord) { em.persist(consumptionRecord);}

    //조회
    public List<ConsumptionRecord> findCrByDeviceId(String deviceId) {
        return em.createQuery("select c from ConsumptionRecord c where c.deviceId = :deviceId", ConsumptionRecord.class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    // 월별 조회
    public List<ConsumptionRecord> findCrByMonth(String deviceId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return em.createQuery("select c from ConsumptionRecord c " +
                                "where c.deviceId = :deviceId " +
                                "and c.consumptionDate between :startDate and :endDate",
                        ConsumptionRecord.class)
                .setParameter("deviceId", deviceId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    // 월별 및 소비 유형별 조회 (이름, 수량, 단위 가격, 총 가격)
    public List<Object[]> findCrByMonthAndType(String deviceId, int year, int month, ConsumptionType consumptionType) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return em.createQuery("select c.foodName, c.quantity, c.price, CAST(c.quantity * c.price AS integer) as totalCost " +
                                "from ConsumptionRecord c " +
                                "where c.deviceId = :deviceId " +
                                "and c.consumptionType = :consumptionType " +
                                "and c.consumptionDate between :startDateTime and :endDateTime",
                        Object[].class)
                .setParameter("deviceId", deviceId)
                .setParameter("consumptionType", consumptionType)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .getResultList();
    }
    // deviceId, foodId, consumptionType을 기반으로 소비 기록 조회
    public List<ConsumptionRecord> findCrByDeviceIdAndFoodIdAndConsumptionType(String deviceId, Integer foodId, ConsumptionType consumptionType) {
        return em.createQuery("SELECT cr FROM ConsumptionRecord cr WHERE cr.deviceId = :deviceId AND cr.foodId = :foodId AND cr.consumptionType = :consumptionType", ConsumptionRecord.class)
                .setParameter("deviceId", deviceId)
                .setParameter("foodId", foodId)
                .setParameter("consumptionType", consumptionType)
                .getResultList();
    }


}
