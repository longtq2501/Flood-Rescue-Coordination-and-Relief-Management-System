package com.floodrescue.request.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.floodrescue.request.domain.enums.RequestStatus;
import com.floodrescue.request.dto.request.CancelRequestDto;
import com.floodrescue.request.dto.request.CreateRescueRequestDto;
import com.floodrescue.request.dto.request.VerifyRequestDto;
import com.floodrescue.request.dto.response.RescueRequestResponse;

public interface RescueRequestService {

    RescueRequestResponse create(CreateRescueRequestDto dto,
            Long citizenId,
            List<MultipartFile> images);

    Page<RescueRequestResponse> getAll(String status,
            String urgencyLevel,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable);

    Page<RescueRequestResponse> getMy(Long citizenId, Pageable pageable);

    RescueRequestResponse getById(Long requestId);

    RescueRequestResponse verify(Long requestId,
            VerifyRequestDto dto,
            Long coordinatorId);

    RescueRequestResponse cancel(Long requestId,
            CancelRequestDto dto,
            Long userId,
            String role);

    RescueRequestResponse confirm(Long requestId, Long citizenId);

    void syncStatus(Long requestId, RequestStatus status,
            String note, Long changedBy);
}
