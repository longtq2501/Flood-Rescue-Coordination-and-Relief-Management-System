package com.floodrescue.module.rescue_request.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.floodrescue.module.rescue_request.dto.request.CancelRequestDto;
import com.floodrescue.module.rescue_request.dto.request.CreateRescueRequestDto;
import com.floodrescue.module.rescue_request.dto.request.VerifyRequestDto;
import com.floodrescue.module.rescue_request.dto.response.RescueRequestResponse;

public interface RescueRequestService {

        /**
         * POST /api/requests
         * Citizen tạo yêu cầu cứu hộ, upload ảnh lên MinIO
         * Sau khi lưu DB → publish rescue.request.created
         */
        RescueRequestResponse create(CreateRescueRequestDto dto,
                        Long citizenId,
                        List<MultipartFile> images);

        /**
         * GET /api/requests — coordinator xem tất cả, có filter
         */
        Page<RescueRequestResponse> getAll(String status,
                        String urgencyLevel,
                        LocalDateTime fromDate,
                        LocalDateTime toDate,
                        Pageable pageable);

        /**
         * GET /api/requests/my — citizen xem yêu cầu của mình
         */
        Page<RescueRequestResponse> getMy(Long citizenId, Pageable pageable);

        /**
         * GET /api/requests/{id} — detail kèm images + status history
         */
        RescueRequestResponse getById(Long requestId);

        /**
         * PATCH /api/requests/{id}/verify — coordinator xét duyệt
         * Đổi status PENDING → VERIFIED
         * Publish rescue.request.status.updated
         */
        RescueRequestResponse verify(Long requestId,
                        VerifyRequestDto dto,
                        Long coordinatorId);

        /**
         * PATCH /api/requests/{id}/cancel — citizen hoặc coordinator hủy
         * Đổi status → CANCELLED
         * Publish rescue.request.status.updated
         */
        RescueRequestResponse cancel(Long requestId,
                        CancelRequestDto dto,
                        Long userId,
                        String role);

        /**
         * PATCH /api/requests/{id}/confirm — citizen xác nhận hoàn thành
         * Đổi status COMPLETED → CONFIRMED
         * Publish rescue.request.status.updated
         */
        RescueRequestResponse confirm(Long requestId, Long citizenId);

        /**
         * Internal: Đồng bộ trạng thái từ các event bên ngoài (Dispatch)
         */
        void syncStatus(Long requestId, com.floodrescue.module.rescue_request.domain.enums.RequestStatus status,
                        String note, Long changedBy);
}