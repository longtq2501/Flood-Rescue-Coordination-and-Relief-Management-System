package com.floodrescue.request.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.floodrescue.request.domain.entity.RequestImage;
import com.floodrescue.request.domain.entity.RescueRequest;
import com.floodrescue.request.domain.entity.StatusHistory;
import com.floodrescue.request.domain.enums.RequestStatus;
import com.floodrescue.request.domain.enums.UrgencyLevel;
import com.floodrescue.request.dto.request.CancelRequestDto;
import com.floodrescue.request.dto.request.CreateRescueRequestDto;
import com.floodrescue.request.dto.request.VerifyRequestDto;
import com.floodrescue.request.dto.response.RescueRequestResponse;
import com.floodrescue.request.dto.response.StatusHistoryResponse;
import com.floodrescue.request.event.RescueRequestCreatedEvent;
import com.floodrescue.request.event.RescueRequestEventPublisher;
import com.floodrescue.request.event.RescueRequestStatusUpdatedEvent;
import com.floodrescue.request.repository.RescueRequestRepository;
import com.floodrescue.request.repository.StatusHistoryRepository;
import com.floodrescue.request.shared.exception.AppException;
import com.floodrescue.request.shared.exception.ErrorCode;
import com.floodrescue.request.shared.util.MinioService;

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

        if (requestRepository.existsByCitizenIdAndStatusIn(citizenId,
                List.of(RequestStatus.PENDING, RequestStatus.VERIFIED,
                        RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS))) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_ACTIVE,
                    "Bạn đang có yêu cầu chưa xử lý. Vui lòng đợi yêu cầu hiện tại được giải quyết.");
        }

        UrgencyLevel urgency = classificationService.classify(dto);
        log.info("Classified urgency for citizenId={}: {}", citizenId, urgency);

        // Upload ảnh lên MinIO TRƯỚC transaction
        List<String> uploadedUrls = new java.util.ArrayList<>();
        try {
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (file != null && !file.isEmpty()) {
                        uploadedUrls.add(minioService.uploadFile(file, "rescue-requests"));
                    }
                }
            }
        } catch (Exception e) {
            uploadedUrls.forEach(minioService::deleteFile);
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Upload ảnh thất bại: " + e.getMessage());
        }

        try {
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

            for (String url : uploadedUrls) {
                RequestImage image = RequestImage.builder()
                        .request(request)
                        .imageUrl(url)
                        .build();
                request.getImages().add(image);
            }

            final RescueRequest savedRequest = requestRepository.save(request);
            log.info("Saved RescueRequest id={} with {} images for citizenId={}",
                    savedRequest.getId(), uploadedUrls.size(), citizenId);

            saveStatusHistory(savedRequest, null, RequestStatus.PENDING,
                    citizenId, "Citizen gửi yêu cầu cứu hộ");

            RescueRequestCreatedEvent event = RescueRequestCreatedEvent.builder()
                    .requestId(savedRequest.getId())
                    .citizenId(citizenId)
                    .lat(savedRequest.getLat())
                    .lng(savedRequest.getLng())
                    .urgencyLevel(urgency.name())
                    .description(savedRequest.getDescription())
                    .numPeople(savedRequest.getNumPeople())
                    .build();
            eventPublisher.publishRequestCreated(event);

            return toResponse(savedRequest);
        } catch (Exception e) {
            log.error("Database transaction failed, cleaning up uploaded files", e);
            uploadedUrls.forEach(minioService::deleteFile);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RescueRequestResponse> getAll(String status,
            String urgencyLevel,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        RequestStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = RequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Trạng thái không hợp lệ: " + status);
            }
        }

        UrgencyLevel urgencyEnum = null;
        if (urgencyLevel != null && !urgencyLevel.isBlank()) {
            try {
                urgencyEnum = UrgencyLevel.valueOf(urgencyLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Mức độ khẩn cấp không hợp lệ: " + urgencyLevel);
            }
        }

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
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Chỉ xét duyệt được yêu cầu ở trạng thái PENDING. Trạng thái hiện tại: "
                            + request.getStatus());
        }

        RequestStatus prevStatus = request.getStatus();
        request.setStatus(RequestStatus.VERIFIED);
        request.setCoordinatorId(coordinatorId);
        request.setVerifiedAt(LocalDateTime.now());

        if (dto.getUrgencyLevel() != null) {
            request.setUrgencyLevel(dto.getUrgencyLevel());
        }

        requestRepository.save(request);

        saveStatusHistory(request, prevStatus, RequestStatus.VERIFIED,
                coordinatorId, dto.getNote());

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
            Long userId,
            String role) {
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        boolean isOwner = userId.equals(request.getCitizenId());
        boolean isStaff = "COORDINATOR".equals(role) || "ADMIN".equals(role);

        if (!isOwner && !isStaff) {
            throw new AppException(ErrorCode.REQUEST_FORBIDDEN,
                    "Bạn không có quyền hủy yêu cầu này");
        }

        if (isStaff && !isOwner) {
            log.info("Staff userId={} role={} cancelling request id={} for citizenId={}",
                    userId, role, requestId, request.getCitizenId());
        }

        RequestStatus current = request.getStatus();
        if (current != RequestStatus.PENDING
                && current != RequestStatus.VERIFIED
                && current != RequestStatus.ASSIGNED) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Không thể hủy yêu cầu ở trạng thái: " + current
                            + ". Chỉ hủy được khi PENDING, VERIFIED hoặc ASSIGNED.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        saveStatusHistory(request, current, RequestStatus.CANCELLED,
                userId, dto.getReason());

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
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        if (!citizenId.equals(request.getCitizenId())) {
            throw new AppException(ErrorCode.REQUEST_FORBIDDEN,
                    "Bạn không có quyền xác nhận yêu cầu này");
        }

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new AppException(ErrorCode.REQUEST_INVALID_STATUS,
                    "Chỉ xác nhận được yêu cầu đã hoàn thành (COMPLETED). Trạng thái hiện tại: "
                            + request.getStatus());
        }

        RequestStatus prevStatus = request.getStatus();
        request.setStatus(RequestStatus.CONFIRMED);
        request.setConfirmedAt(LocalDateTime.now());
        requestRepository.save(request);

        saveStatusHistory(request, prevStatus, RequestStatus.CONFIRMED,
                citizenId, "Citizen xác nhận đã được cứu hộ thành công");

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

    @Override
    @Transactional
    public void syncStatus(Long requestId, RequestStatus status, String note, Long changedBy) {
        RescueRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND,
                        "Không tìm thấy yêu cầu cứu hộ id=" + requestId));

        RequestStatus prevStatus = request.getStatus();

        if (prevStatus == status) {
            return;
        }

        log.info("Syncing RescueRequest id={} status: {} -> {}", requestId, prevStatus, status);

        request.setStatus(status);
        if (status == RequestStatus.COMPLETED) {
            request.setCompletedAt(LocalDateTime.now());
        }
        requestRepository.save(request);

        saveStatusHistory(request, prevStatus, status, changedBy, note);

        eventPublisher.publishStatusUpdated(RescueRequestStatusUpdatedEvent.builder()
                .requestId(request.getId())
                .citizenId(request.getCitizenId())
                .fromStatus(prevStatus.name())
                .toStatus(status.name())
                .changedBy(changedBy)
                .note(note)
                .build());
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
        List<String> imageUrls = request.getImages() == null
                ? List.of()
                : request.getImages().stream()
                        .map(img -> minioService.getPresignedUrl(img.getImageUrl()))
                        .collect(Collectors.toList());

        List<StatusHistoryResponse> histories = request.getStatusHistories() == null
                ? List.of()
                : request.getStatusHistories().stream()
                        .sorted(Comparator.comparing(
                                StatusHistory::getChangedAt,
                                Comparator.nullsFirst(
                                        Comparator.naturalOrder())))
                        .map(h -> StatusHistoryResponse.builder()
                                .fromStatus(h.getFromStatus())
                                .toStatus(h.getToStatus())
                                .changedBy(h.getChangedBy())
                                .note(h.getNote())
                                .changedAt(h.getChangedAt())
                                .build())
                        .collect(Collectors.toList());

        return RescueRequestResponse.builder()
                .id(request.getId())
                .citizenId(request.getCitizenId())
                .lat(request.getLat())
                .lng(request.getLng())
                .addressText(request.getAddressText())
                .description(request.getDescription())
                .numPeople(request.getNumPeople())
                .urgencyLevel(request.getUrgencyLevel())
                .status(request.getStatus())
                .coordinatorId(request.getCoordinatorId())
                .imageUrls(imageUrls)
                .statusHistories(histories)
                .verifiedAt(request.getVerifiedAt())
                .completedAt(request.getCompletedAt())
                .confirmedAt(request.getConfirmedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
