package com.floodrescue.request.service;

import org.springframework.stereotype.Service;

import com.floodrescue.request.domain.enums.UrgencyLevel;
import com.floodrescue.request.dto.request.CreateRescueRequestDto;

@Service
public class UrgencyClassificationService {

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
