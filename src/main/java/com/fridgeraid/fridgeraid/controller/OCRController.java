package com.fridgeraid.fridgeraid.controller;

import com.fridgeraid.fridgeraid.service.VisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class OCRController {

    private final VisionService visionService;

    // 이미지 파일로부터 텍스트 추출
    @PostMapping("/extract-text")
    public String extractText(@RequestParam("imageFile") MultipartFile imageFile) {
        try {
            return visionService.extractTextFromImageFile(imageFile);
        } catch (Exception e) {
            return "Failed to extract text: " + e.getMessage();
        }
    }
}
