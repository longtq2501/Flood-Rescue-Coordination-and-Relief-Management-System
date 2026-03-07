package com.floodrescue.module.rescue_request.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.floodrescue.module.rescue_request.domain.entity.RequestImage;
import com.floodrescue.module.rescue_request.domain.entity.RescueRequest;
import com.floodrescue.module.rescue_request.domain.entity.StatusHistory;
import com.floodrescue.module.rescue_request.domain.enums.RequestStatus;
import com.floodrescue.module.rescue_request.domain.enums.UrgencyLevel;
import com.floodrescue.module.rescue_request.dto.response.StatusHistoryResponse;
import com.floodrescue.module.rescue_request.event.RescueRequestCreatedEvent;
import com.floodrescue.module.rescue_request.event.RescueRequestStatusUpdatedEvent;
import com.floodrescue.module.rescue_request.dto.request.CancelRequestDto;
import com.floodrescue.module.rescue_request.dto.request.CreateRescueRequestDto;
import com.floodrescue.module.rescue_request.dto.request.VerifyRequestDto;
import com.floodrescue.module.rescue_request.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue_request.event.RescueRequestEventPublisher;
import com.floodrescue.module.rescue_request.repository.RescueRequestRepository;
import com.floodrescue.module.rescue_request.repository.StatusHistoryRepository;
import com.floodrescue.shared.exception.AppException;
import com.floodrescue.shared.exception.ErrorCode;
import com.floodrescue.shared.util.MinioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RescueRequestServiceImpl implements RescueRequestService {

    private final RescueRequestRepository requestRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UrgencyClassificationService classificationService;
    private final RescueRequestEventPublisher eventPublisher;
    private final MinioService minioService;

    // ==================== WRITE OPERATIONS ====================

    @Override
    @Transactional
    public RescueRequestResponse create(CreateRescueRequestDto dto,
            Long citizenId,
            List<MultipartFile> images) {

        // Step 1: Kiểm tra citizen không có request đang hoạt động
        if (requestRepository.existsByCitizenIdAndStatusIn(citizenId,
                List.of(RequestStatus.PENDING, RequestStatus.VERIFIED,
                        RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS))) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_ACTIVE,
                    "Bạn đang có yêu cầu chưa xử lý. Vui lòng đợi yêu cầu hiện tại được giải quyết.");
        }

        // Step 2: Phân loại urgency tự động
        UrgencyLevel urgency = classificationService.classify(dto);
        log.info("Classified urgency for citizenId={}: {}", citizenId, urgency);

        // Step 3: Tạo RescueRequest entity và lưu DB
        RescueRequest request = RescueRequest.builder()
                .citizenId(citizenId)
                .lat(dto.getLat())
                .lng(dto.getLng())
                .addressText(dto.getAddressText())
                .description(dto.getDescription())
                .numPeople(dto.getNumPeople() != null ? dto.getNumPeople() : 1)
                .urgencyLevel(urgency)
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        log.info("Saved RescueRequest id={} for citizenId={}", request.getId(), citizenId);

        // Step 4: Upload ảnh lên MinIO (nếu có)
        // → minioService.uploadFile(file, "rescue-requests") → trả về objectName
        // → tạo RequestImage entity cho mỗi ảnh, lưu DB
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String objectName = minioService.uploadFile(file, "rescue-requests");
                    RequestImage image = RequestImage.builder()
                            .request(request)
                            .imageUrl(objectName)
                            .build();
                    request.getImages().add(image);
                }
            }
        }

        // Step 5: Lưu StatusHistory đầu tiên (PENDING)
        saveStatusHistory(request, null, RequestStatus.PENDING,
                citizenId, "Citizen gửi yêu cầu cứu hộ");

        // Step 6: Publish event
        // → eventPublisher.publishRequestCreated(...)
        RescueRequestCreatedEvent event = RescueRequestCreatedEvent.builder()
                .requestId(request.getId())
                .citizenId(citizenId)
                .citizenName("Citizen_" + citizenId)
                .lat(request.getLat())
                .lng(request.getLng())
                .urgencyLevel(urgency.name())
                .description(request.getDescription())
                .numPeople(request.getNumPeople())
                .build();
        eventPublisher.publishRequestCreated(event);

        // Step 7: Trả về RescueRequestResponse
        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getAll(String status,
            String urgencyLevel,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        // Gợi ý: parse String → Enum (null nếu không có)
        RequestStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = RequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Trạng thái không hợp lệ: " + status);
            }
        }

        UrgencyLevel urgencyEnum = null;
        if (urgencyLevel != null && !urgencyLevel.isBlank()) {
            try {
                urgencyEnum = UrgencyLevel.valueOf(urgencyLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Mức độ khẩn cấp không hợp lệ: " + urgencyLevel);
            }
        }

        // → requestRepository.findAllWithFilters(...)
        // → map sang RescueRequestResponse
        return requestRepository
                .findAllWithFilters(statusEnum, urgencyEnum, fromDate, toDate, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getMy(Long citizenId, Pageable pageable) {
        return requestRepository
                .findByCitizenId(citizenId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RescueRequestResponse getById(Long requestId) {
        // Gợi ý: dùng requestRepository.findByIdWithDetails(requestId)
        // map images → presigned URLs qua minioService.getPresignedUrl(objectName)
        RescueRequest request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));
        return toResponse(request);
    }

    @Override
    @Transactional
    public RescueRequestResponse verify(Long requestId,
            VerifyRequestDto dto,
            Long coordinatorId) {
        // Step 1: Tìm request, kiểm tra status == PENDING → throw nếu không đúng
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Chỉ xét duyệt được yêu cầu ở trạng thái PENDING. Trạng thái hiện tại: "
                            + request.getStatus());
        }

        // Step 2: Đổi status → VERIFIED, set coordinatorId, verifiedAt
        RequestStatus prevStatus = request.getStatus();
        request.setStatus(RequestStatus.VERIFIED);
        request.setCoordinatorId(coordinatorId);
        request.setVerifiedAt(LocalDateTime.now());

        // Step 3: Nếu dto.urgencyLevel != null → override urgency
        if (dto.getUrgencyLevel() != null) {
            request.setUrgencyLevel(dto.getUrgencyLevel());
        }

        requestRepository.save(request);

        // Step 4: Lưu StatusHistory (PENDING → VERIFIED)
        saveStatusHistory(request, prevStatus, RequestStatus.VERIFIED,
                coordinatorId, dto.getNote());

        // Step 5: Publish rescue.request.status.updated
        eventPublisher.publishStatusUpdated(RescueRequestStatusUpdatedEvent.builder()
                .requestId(request.getId())
                .citizenId(request.getCitizenId())
                .fromStatus(prevStatus.name())
                .toStatus(RequestStatus.VERIFIED.name())
                .changedBy(coordinatorId)
                .note(dto.getNote())
                .build());

        return toResponse(request);
    }

    @Override
    @Transactional
    public RescueRequestResponse cancel(Long requestId,
            CancelRequestDto dto,
            Long userId) {
        // TODO Cường: implement
        // Step 1: Tìm request
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        // Step 2: Kiểm tra quyền — citizen chỉ cancel được request của mình
        // coordinator cancel được bất kỳ request nào
        if (!userId.equals(request.getCitizenId())) {
            log.info("Coordinator userId={} cancelling request id={}", userId, requestId);
        }

        // Step 3: Kiểm tra status — chỉ cancel được khi PENDING, VERIFIED, ASSIGNED
        RequestStatus current = request.getStatus();
        if (current != RequestStatus.PENDING
                && current != RequestStatus.VERIFIED
                && current != RequestStatus.ASSIGNED) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Không thể hủy yêu cầu ở trạng thái: " + current
                            + ". Chỉ hủy được khi PENDING, VERIFIED hoặc ASSIGNED.");
        }

        // Step 4: Đổi status → CANCELLED
        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        // Step 5: Lưu StatusHistory
        saveStatusHistory(request, current, RequestStatus.CANCELLED,
                userId, dto.getReason());

        // Step 6: Publish event
        eventPublisher.publishStatusUpdated(RescueRequestStatusUpdatedEvent.builder()
                .requestId(request.getId())
                .citizenId(request.getCitizenId())
                .fromStatus(current.name())
                .toStatus(RequestStatus.CANCELLED.name())
                .changedBy(userId)
                .note(dto.getReason())
                .build());

        return toResponse(request);
    }

    @Override
    @Transactional
    public RescueRequestResponse confirm(Long requestId, Long citizenId) {
        // Step 1: Tìm request, kiểm tra citizenId == request.citizenId
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        if (!citizenId.equals(request.getCitizenId())) {
            throw new AppException(ErrorCode.REQUEST_FORBIDDEN,
                    "Bạn không có quyền xác nhận yêu cầu này");
        }

        // Step 2: Kiểm tra status == COMPLETED
        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Chỉ xác nhận được yêu cầu đã hoàn thành (COMPLETED). Trạng thái hiện tại: "
                            + request.getStatus());
        }

        // Step 3: Đổi status → CONFIRMED, set confirmedAt
        RequestStatus prevStatus = request.getStatus();
        request.setStatus(RequestStatus.CONFIRMED);
        request.setConfirmedAt(LocalDateTime.now());
        requestRepository.save(request);

        // Step 4: Lưu StatusHistory
        saveStatusHistory(request, prevStatus, RequestStatus.CONFIRMED,
                citizenId, "Citizen xác nhận đã được cứu hộ thành công");

        // Step 5: Publish event
        eventPublisher.publishStatusUpdated(RescueRequestStatusUpdatedEvent.builder()
                .requestId(request.getId())
                .citizenId(request.getCitizenId())
                .fromStatus(prevStatus.name())
                .toStatus(RequestStatus.CONFIRMED.name())
                .changedBy(citizenId)
                .note("Citizen xác nhận đã được cứu hộ thành công")
                .build());

        return toResponse(request);
    }

    // ==================== PRIVATE HELPERS ====================

    private void saveStatusHistory(RescueRequest request,
            RequestStatus fromStatus,
            RequestStatus toStatus,
            Long changedBy,
            String note) {
        StatusHistory history = StatusHistory.builder()
                .request(request)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .note(note)
                .build();
        statusHistoryRepository.save(history);
    }

    private RescueRequestResponse toResponse(RescueRequest request) {
        // TODO Cường: implement mapping
        // Gợi ý: map images → minioService.getPresignedUrl(image.getImageUrl())
        // map statusHistories → StatusHistoryResponse
        throw new UnsupportedOperationException("TODO: Cường implement toResponse()");
    }
}