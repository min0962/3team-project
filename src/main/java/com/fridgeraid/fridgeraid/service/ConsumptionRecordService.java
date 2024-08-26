package com.fridgeraid.fridgeraid.service;

import com.fridgeraid.fridgeraid.Repository.ConsumptionRecordRepository;
import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsumptionRecordService {

    private final ConsumptionRecordRepository consumptionRecordRepository;

    //저장
    @Transactional
    public void saveConsumptionRecord(ConsumptionRecord consumptionRecord) {
        consumptionRecordRepository.save(consumptionRecord);
    }

    //월단위 조회
    public List<ConsumptionRecord> findConsumptionRecordsByMonth(String deviceId, int year, int month) {
        return consumptionRecordRepository.findCrByMonth(deviceId, year, month);
    }

    // 월별 및 소비 유형별 조회 (이름, 수량, 가격)
    public List<Object[]> getMonthlyConsumptionByType(String deviceId, int year, int month, ConsumptionType consumptionType) {
        return consumptionRecordRepository.findCrByMonthAndType(deviceId, year, month, consumptionType);
    }
}
