package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.domain.ConsumptionRecord;
import com.fridgeraid.fridgeraid.domain.ConsumptionType;
import com.fridgeraid.fridgeraid.service.ConsumptionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumption-records")
@RequiredArgsConstructor
public class ConsumptionRecordController {

    private final ConsumptionRecordService consumptionRecordService;

    // 월단위 조회
    @GetMapping("/{deviceId}/month")
    public List<ConsumptionRecord> getConsumptionRecordsByMonth(
            @PathVariable String deviceId,
            @RequestParam int year,
            @RequestParam int month) {
        return consumptionRecordService.findConsumptionRecordsByMonth(deviceId, year, month);
    }

    // 월별 및 소비 유형별 조회 (이름, 수량, 단가)
    @GetMapping("/{deviceId}/month/type")
    public List<Object[]> getMonthlyConsumptionByType(
            @PathVariable String deviceId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam ConsumptionType consumptionType) {
        return consumptionRecordService.getMonthlyConsumptionByType(deviceId, year, month, consumptionType);
    }

    // 월별 소비 및 지출 분석
    @GetMapping("/{deviceId}/month/consumption-analysis")
    public ResponseEntity<String> analyzeMonthlyConsumption(
            @PathVariable String deviceId,
            @RequestParam int year,
            @RequestParam int month) {
        String response = consumptionRecordService.analyzeMonthlyConsumption(deviceId, year, month);
        return ResponseEntity.ok(response);
    }

    // 월별 폐기물 분석
    @GetMapping("/{deviceId}/month/waste-analysis")
    public ResponseEntity<String> analyzeMonthlyWaste(
            @PathVariable String deviceId,
            @RequestParam int year,
            @RequestParam int month) {
        String response = consumptionRecordService.analyzeMonthlyWaste(deviceId, year, month);
        return ResponseEntity.ok(response);
    }
}
