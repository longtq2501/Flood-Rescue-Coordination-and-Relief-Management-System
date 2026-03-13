import com.floodrescue.request.domain.entity.RequestImage;
import com.floodrescue.request.domain.entity.RescueRequest;
import com.floodrescue.request.domain.entity.StatusHistory;
import com.floodrescue.request.domain.enums.RequestStatus;
import com.floodrescue.request.domain.enums.UrgencyLevel;
import com.floodrescue.request.dto.request.CancelRequestDto;
import com.floodrescue.request.dto.request.CreateRescueRequestDto;
import com.floodrescue.request.dto.request.VerifyRequestDto;
import com.floodrescue.request.dto.response.RescueRequestResponse;
import com.floodrescue.request.event.RescueRequestCreatedEvent;
import com.floodrescue.request.event.RescueRequestEventPublisher;
import com.floodrescue.request.event.RescueRequestStatusUpdatedEvent;
import com.floodrescue.request.repository.RescueRequestRepository;
import com.floodrescue.request.repository.StatusHistoryRepository;
import com.floodrescue.request.service.RescueRequestServiceImpl;
import com.floodrescue.request.service.UrgencyClassificationService;
import com.floodrescue.request.shared.exception.AppException;
import com.floodrescue.request.shared.exception.ErrorCode;
import com.floodrescue.request.shared.util.MinioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueRequestServiceImplTest {

    @Mock private RescueRequestRepository requestRepository;
    @Mock private StatusHistoryRepository statusHistoryRepository;
    @Mock private UrgencyClassificationService classificationService;
    @Mock private RescueRequestEventPublisher eventPublisher;
    @Mock private MinioService minioService;

    @InjectMocks private RescueRequestServiceImpl rescueRequestService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private RescueRequest buildRequest(Long id, Long citizenId, RequestStatus status) {
        return RescueRequest.builder()
                .id(id)
                .citizenId(citizenId)
                .lat(BigDecimal.valueOf(10.762622))
                .lng(BigDecimal.valueOf(106.660172))
                .addressText("123 Test Street")
                .description("Need rescue")
                .numPeople(2)
                .urgencyLevel(UrgencyLevel.HIGH)
                .status(status)
                .images(new ArrayList<>())
                .statusHistories(new ArrayList<>())
                .build();
    }

    private CreateRescueRequestDto buildCreateDto() {
        CreateRescueRequestDto dto = new CreateRescueRequestDto();
        dto.setLat(BigDecimal.valueOf(10.762622));
        dto.setLng(BigDecimal.valueOf(106.660172));
        dto.setAddressText("123 Test Street");
        dto.setDescription("Need rescue");
        dto.setNumPeople(2);
        return dto;
    }

    private MockMultipartFile buildMockImage(String name) {
        return new MockMultipartFile(name, name + ".jpg",
                "image/jpeg", "fake-image-content".getBytes());
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create request, save status history and publish event when valid")
        void success_noImages() {
            // ARRANGE
            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(false);
            when(classificationService.classify(any())).thenReturn(UrgencyLevel.HIGH);
            when(requestRepository.save(any(RescueRequest.class)))
                    .thenAnswer(inv -> {
                        RescueRequest r = inv.getArgument(0);
                        r.setId(1L);
                        return r;
                    });
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT
            RescueRequestResponse response = rescueRequestService.create(buildCreateDto(), 1L, List.of());

            // ASSERT
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(response.getUrgencyLevel()).isEqualTo(UrgencyLevel.HIGH);

            // VERIFY — status history must be saved with correct transition
            ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getFromStatus()).isNull(); // first status, no previous
            assertThat(historyCaptor.getValue().getToStatus()).isEqualTo(RequestStatus.PENDING);

            // VERIFY — created event must be published
            verify(eventPublisher).publishRequestCreated(any(RescueRequestCreatedEvent.class));
        }

        @Test
        @DisplayName("should upload images to MinIO and attach URLs to request when images provided")
        void success_withImages() {
            // ARRANGE
            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(false);
            when(classificationService.classify(any())).thenReturn(UrgencyLevel.MEDIUM);
            when(minioService.uploadFile(any(), eq("rescue-requests")))
                    .thenReturn("https://minio/rescue-requests/img1.jpg");
            when(requestRepository.save(any())).thenAnswer(inv -> {
                RescueRequest r = inv.getArgument(0);
                r.setId(2L);
                return r;
            });
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(minioService.getPresignedUrl(anyString())).thenReturn("https://presigned/img1.jpg");

            List<MultipartFile> images = List.of(buildMockImage("photo1"));

            // ACT
            RescueRequestResponse response = rescueRequestService.create(buildCreateDto(), 1L, images);

            // ASSERT — presigned URL must appear in response
            assertThat(response.getImageUrls()).hasSize(1);
            assertThat(response.getImageUrls().getFirst()).isEqualTo("https://presigned/img1.jpg");

            verify(minioService).uploadFile(any(), eq("rescue-requests"));
        }

        @Test
        @DisplayName("should throw REQUEST_ALREADY_ACTIVE when citizen has an active request")
        void activeRequestExists_shouldThrow() {
            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(true);

            assertThatThrownBy(() -> rescueRequestService.create(buildCreateDto(), 1L, List.of()))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_ALREADY_ACTIVE);

            // No request saved, no event published
            verify(requestRepository, never()).save(any());
            verify(eventPublisher, never()).publishRequestCreated(any());
        }

        @Test
        @DisplayName("should delete already-uploaded files and throw when MinIO upload fails mid-way")
        void minioUploadFails_shouldCleanupAndThrow() {
            // ARRANGE — first file uploads OK, second throws IOException
            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(false);
            when(classificationService.classify(any())).thenReturn(UrgencyLevel.LOW);
            when(minioService.uploadFile(any(), anyString()))
                    .thenReturn("https://minio/file1.jpg")            // first file OK
                    .thenThrow(new RuntimeException("MinIO connection refused")); // second fails

            List<MultipartFile> images = List.of(
                    buildMockImage("photo1"),
                    buildMockImage("photo2")
            );

            // ASSERT
            assertThatThrownBy(() -> rescueRequestService.create(buildCreateDto(), 1L, images))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            // VERIFY — the already-uploaded file must be deleted to avoid orphaned files in MinIO
            verify(minioService).deleteFile("https://minio/file1.jpg");
            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("should delete uploaded files and rethrow when DB save fails after upload")
        void dbSaveFails_shouldCleanupUploadedFiles() {
            // ARRANGE — images uploaded successfully, but DB throws
            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(false);
            when(classificationService.classify(any())).thenReturn(UrgencyLevel.HIGH);
            when(minioService.uploadFile(any(), anyString()))
                    .thenReturn("https://minio/file1.jpg");
            when(requestRepository.save(any()))
                    .thenThrow(new RuntimeException("DB connection lost"));

            List<MultipartFile> images = List.of(buildMockImage("photo1"));

            // ASSERT
            assertThatThrownBy(() -> rescueRequestService.create(buildCreateDto(), 1L, images))
                    .isInstanceOf(RuntimeException.class);

            // VERIFY — file uploaded before DB failure must be deleted
            verify(minioService).deleteFile("https://minio/file1.jpg");
        }

        @Test
        @DisplayName("should default numPeople to 1 when dto.numPeople is null")
        void nullNumPeople_shouldDefaultToOne() {
            CreateRescueRequestDto dto = buildCreateDto();
            dto.setNumPeople(null); // explicitly null

            when(requestRepository.existsByCitizenIdAndStatusIn(eq(1L), anyList()))
                    .thenReturn(false);
            when(classificationService.classify(any())).thenReturn(UrgencyLevel.LOW);
            when(requestRepository.save(any())).thenAnswer(inv -> {
                RescueRequest r = inv.getArgument(0);
                r.setId(1L);
                return r;
            });
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RescueRequestResponse response = rescueRequestService.create(dto, 1L, List.of());

            assertThat(response.getNumPeople()).isEqualTo(1);
        }
    }

    // =====================================================================
    // getAll()
    // =====================================================================

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should return paged results when filters are valid")
        void success() {
            RescueRequest request = buildRequest(1L, 1L, RequestStatus.PENDING);
            Pageable pageable = PageRequest.of(0, 10);
            when(requestRepository.findAllWithFilters(any(), any(), any(), any(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(request), pageable, 1));

            Page<RescueRequestResponse> result = rescueRequestService
                    .getAll("PENDING", null, null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getStatus()).isEqualTo(RequestStatus.PENDING);
        }

        @Test
        @DisplayName("should pass null status enum when status filter is null")
        void nullStatus_shouldPassNullEnum() {
            Pageable pageable = PageRequest.of(0, 10);
            when(requestRepository.findAllWithFilters(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            rescueRequestService.getAll(null, null, null, null, pageable);

            verify(requestRepository).findAllWithFilters(null, null, null, null, pageable);
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when status string is invalid")
        void invalidStatus_shouldThrow() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> rescueRequestService
                    .getAll("INVALID_STATUS", null, null, null, pageable))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when urgencyLevel string is invalid")
        void invalidUrgencyLevel_shouldThrow() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> rescueRequestService
                    .getAll(null, "NOT_A_LEVEL", null, null, pageable))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
        }
    }

    // =====================================================================
    // getById()
    // =====================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return response with presigned image URLs when request exists")
        void success() {
            RescueRequest request = buildRequest(1L, 1L, RequestStatus.PENDING);
            RequestImage image = RequestImage.builder()
                    .imageUrl("rescue-requests/img1.jpg").build();
            request.getImages().add(image);

            when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(request));
            when(minioService.getPresignedUrl("rescue-requests/img1.jpg"))
                    .thenReturn("https://presigned/img1.jpg");

            RescueRequestResponse response = rescueRequestService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getImageUrls()).containsExactly("https://presigned/img1.jpg");
        }

        @Test
        @DisplayName("should throw REQUEST_NOT_FOUND when request does not exist")
        void notFound_shouldThrow() {
            when(requestRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rescueRequestService.getById(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_FOUND);
        }
    }

    // =====================================================================
    // verify()
    // =====================================================================

    @Nested
    @DisplayName("verify()")
    class Verify {

        @Test
        @DisplayName("should set VERIFIED status, set coordinatorId and publish event")
        void success() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.PENDING);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VerifyRequestDto dto = new VerifyRequestDto();
            dto.setUrgencyLevel(UrgencyLevel.CRITICAL);
            dto.setNote("Verified by coordinator");

            RescueRequestResponse response = rescueRequestService.verify(1L, dto, 99L);

            assertThat(response.getStatus()).isEqualTo(RequestStatus.VERIFIED);
            assertThat(response.getCoordinatorId()).isEqualTo(99L);
            assertThat(response.getUrgencyLevel()).isEqualTo(UrgencyLevel.CRITICAL);
            assertThat(response.getVerifiedAt()).isNotNull();

            // VERIFY — status history must record PENDING → VERIFIED transition
            ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getFromStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(historyCaptor.getValue().getToStatus()).isEqualTo(RequestStatus.VERIFIED);

            verify(eventPublisher).publishStatusUpdated(any(RescueRequestStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("should keep original urgencyLevel when dto.urgencyLevel is null")
        void nullUrgencyInDto_shouldKeepOriginal() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.PENDING);
            // urgencyLevel is HIGH from builder
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VerifyRequestDto dto = new VerifyRequestDto();
            dto.setUrgencyLevel(null); // coordinator did not change urgency
            dto.setNote("ok");

            RescueRequestResponse response = rescueRequestService.verify(1L, dto, 99L);

            // urgencyLevel must remain unchanged
            assertThat(response.getUrgencyLevel()).isEqualTo(UrgencyLevel.HIGH);
        }

        @Test
        @DisplayName("should throw REQUEST_NOT_FOUND when request does not exist")
        void notFound_shouldThrow() {
            when(requestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rescueRequestService
                    .verify(99L, new VerifyRequestDto(), 1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("should throw REQUEST_INVALID_STATUS when request is not PENDING")
        void notPending_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.VERIFIED); // already verified
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> rescueRequestService
                    .verify(1L, new VerifyRequestDto(), 99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_INVALID_STATUS);

            verify(requestRepository, never()).save(any());
            verify(eventPublisher, never()).publishStatusUpdated(any());
        }
    }

    // =====================================================================
    // cancel()
    // =====================================================================

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        private CancelRequestDto buildCancelDto(String reason) {
            CancelRequestDto dto = new CancelRequestDto();
            dto.setReason(reason);
            return dto;
        }

        @Test
        @DisplayName("should cancel when citizen is the owner and status is PENDING")
        void citizenOwner_pendingStatus_success() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.PENDING);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RescueRequestResponse response = rescueRequestService
                    .cancel(1L, buildCancelDto("Changed my mind"), 10L, "CITIZEN");

            assertThat(response.getStatus()).isEqualTo(RequestStatus.CANCELLED);
            verify(eventPublisher).publishStatusUpdated(any(RescueRequestStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("should allow COORDINATOR to cancel another citizen's request")
        void staffCoordinator_canCancelOthersCitizenRequest() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.VERIFIED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // coordinator (userId=99) cancels citizen's (id=10) request
            RescueRequestResponse response = rescueRequestService
                    .cancel(1L, buildCancelDto("Duplicate request"), 99L, "COORDINATOR");

            assertThat(response.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("should allow ADMIN to cancel any request")
        void staffAdmin_canCancelAnyRequest() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.ASSIGNED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RescueRequestResponse response = rescueRequestService
                    .cancel(1L, buildCancelDto("Admin override"), 1L, "ADMIN");

            assertThat(response.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw REQUEST_FORBIDDEN when non-owner citizen tries to cancel")
        void nonOwnerCitizen_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.PENDING);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            // userId=99 is neither the owner (10) nor staff
            assertThatThrownBy(() -> rescueRequestService
                    .cancel(1L, buildCancelDto("reason"), 99L, "CITIZEN"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_FORBIDDEN);

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw REQUEST_INVALID_STATUS when status is IN_PROGRESS")
        void inProgressStatus_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.IN_PROGRESS);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> rescueRequestService
                    .cancel(1L, buildCancelDto("reason"), 10L, "CITIZEN"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_INVALID_STATUS);
        }

        @Test
        @DisplayName("should throw REQUEST_INVALID_STATUS when status is COMPLETED")
        void completedStatus_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.COMPLETED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> rescueRequestService
                    .cancel(1L, buildCancelDto("reason"), 10L, "CITIZEN"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_INVALID_STATUS);
        }

        @Test
        @DisplayName("should record correct fromStatus → CANCELLED in status history")
        void shouldRecordCorrectStatusTransition() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.VERIFIED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            rescueRequestService.cancel(1L, buildCancelDto("reason"), 10L, "CITIZEN");

            ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);
            verify(statusHistoryRepository).save(captor.capture());
            assertThat(captor.getValue().getFromStatus()).isEqualTo(RequestStatus.VERIFIED);
            assertThat(captor.getValue().getToStatus()).isEqualTo(RequestStatus.CANCELLED);
        }
    }

    // =====================================================================
    // confirm()
    // =====================================================================

    @Nested
    @DisplayName("confirm()")
    class Confirm {

        @Test
        @DisplayName("should set CONFIRMED status and confirmedAt when citizen is owner and status is COMPLETED")
        void success() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.COMPLETED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RescueRequestResponse response = rescueRequestService.confirm(1L, 10L);

            assertThat(response.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
            assertThat(response.getConfirmedAt()).isNotNull();
            verify(eventPublisher).publishStatusUpdated(any(RescueRequestStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("should throw REQUEST_FORBIDDEN when confirming citizen is not the owner")
        void notOwner_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.COMPLETED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            // citizenId=99 is not the owner (10)
            assertThatThrownBy(() -> rescueRequestService.confirm(1L, 99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_FORBIDDEN);

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw REQUEST_INVALID_STATUS when request is not COMPLETED")
        void notCompleted_shouldThrow() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.IN_PROGRESS);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> rescueRequestService.confirm(1L, 10L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_INVALID_STATUS);
        }
    }

    // =====================================================================
    // syncStatus()
    // =====================================================================

    @Nested
    @DisplayName("syncStatus()")
    class SyncStatus {

        @Test
        @DisplayName("should update status, save history and publish event when status changes")
        void success_statusChanges() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.ASSIGNED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            rescueRequestService.syncStatus(1L, RequestStatus.IN_PROGRESS, "Team started", 99L);

            ArgumentCaptor<RescueRequest> requestCaptor = ArgumentCaptor.forClass(RescueRequest.class);
            verify(requestRepository).save(requestCaptor.capture());
            assertThat(requestCaptor.getValue().getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);

            verify(statusHistoryRepository).save(any());
            verify(eventPublisher).publishStatusUpdated(any(RescueRequestStatusUpdatedEvent.class));
        }

        @Test
        @DisplayName("should set completedAt when syncing status to COMPLETED")
        void syncToCompleted_shouldSetCompletedAt() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.IN_PROGRESS);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            rescueRequestService.syncStatus(1L, RequestStatus.COMPLETED, "Mission done", 99L);

            ArgumentCaptor<RescueRequest> captor = ArgumentCaptor.forClass(RescueRequest.class);
            verify(requestRepository).save(captor.capture());
            assertThat(captor.getValue().getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should do nothing when new status equals current status (idempotent)")
        void sameStatus_shouldSkipAllSideEffects() {
            // This is an important guard — prevents duplicate events when message is redelivered
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.IN_PROGRESS);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

            rescueRequestService.syncStatus(1L, RequestStatus.IN_PROGRESS, "duplicate event", 99L);

            // No save, no history, no event when status hasn't actually changed
            verify(requestRepository, never()).save(any());
            verify(statusHistoryRepository, never()).save(any());
            verify(eventPublisher, never()).publishStatusUpdated(any());
        }

        @Test
        @DisplayName("should throw REQUEST_NOT_FOUND when request does not exist")
        void notFound_shouldThrow() {
            when(requestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rescueRequestService
                    .syncStatus(99L, RequestStatus.COMPLETED, "note", 1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_FOUND);
        }

        @Test
        @DisplayName("should publish event with correct fromStatus and toStatus")
        void shouldPublishEventWithCorrectStatusTransition() {
            RescueRequest request = buildRequest(1L, 10L, RequestStatus.VERIFIED);
            when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
            when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            rescueRequestService.syncStatus(1L, RequestStatus.ASSIGNED, "Team assigned", 99L);

            ArgumentCaptor<RescueRequestStatusUpdatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(RescueRequestStatusUpdatedEvent.class);
            verify(eventPublisher).publishStatusUpdated(eventCaptor.capture());

            RescueRequestStatusUpdatedEvent event = eventCaptor.getValue();
            assertThat(event.getFromStatus()).isEqualTo("VERIFIED");
            assertThat(event.getToStatus()).isEqualTo("ASSIGNED");
            assertThat(event.getChangedBy()).isEqualTo(99L);
        }
    }
}