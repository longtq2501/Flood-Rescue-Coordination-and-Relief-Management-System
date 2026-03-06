package com.floodrescue.module.rescue_request.service;

import org.springframework.stereotype.Service;

import com.floodrescue.module.rescue_request.domain.enums.UrgencyLevel;
import com.floodrescue.module.rescue_request.dto.request.CreateRescueRequestDto;

@Service
public class UrgencyClassificationService {

    /**
     * Phân loại mức độ khẩn cấp dựa trên nội dung mô tả và số người.
     * TODO Cường: có thể bổ sung thêm rule nếu muốn.
     *
     * Rule hiện tại:
     * - Có keyword nguy hiểm tính mạng + numPeople >= 3 → CRITICAL
     * - Có keyword nguy hiểm tính mạng → HIGH
     * - numPeople >= 5 → HIGH
     * - numPeople >= 3 → MEDIUM
     * - Còn lại → LOW
     */
    public UrgencyLevel classify(CreateRescueRequestDto dto) {
        String desc = dto.getDescription().toLowerCase();
        int numPeople = dto.getNumPeople() != null ? dto.getNumPeople() : 1;

        boolean lifeThreat = containsLifeThreatKeyword(desc);

        if (lifeThreat && numPeople >= 3)
            return UrgencyLevel.CRITICAL;
        if (lifeThreat)
            return UrgencyLevel.HIGH;
        if (numPeople >= 5)
            return UrgencyLevel.HIGH;
        if (numPeople >= 3)
            return UrgencyLevel.MEDIUM;
        return UrgencyLevel.LOW;
    }

    private boolean containsLifeThreatKeyword(String desc) {
        return desc.contains("chết")
                || desc.contains("chìm")
                || desc.contains("ngập")
                || desc.contains("kẹt")
                || desc.contains("mắc kẹt")
                || desc.contains("trẻ em")
                || desc.contains("người già")
                || desc.contains("bệnh")
                || desc.contains("thương")
                || desc.contains("cấp cứu");
    }
}