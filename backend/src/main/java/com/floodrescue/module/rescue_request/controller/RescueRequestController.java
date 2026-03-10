package com.floodrescue.module.rescue_request.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.floodrescue.module.rescue_request.dto.request.CancelRequestDto;
import com.floodrescue.module.rescue_request.dto.request.CreateRescueRequestDto;
import com.floodrescue.module.rescue_request.dto.request.VerifyRequestDto;
import com.floodrescue.module.rescue_request.dto.response.RescueRequestResponse;
import com.floodrescue.module.rescue_request.service.RescueRequestService;
import com.floodrescue.shared.response.ApiResponse;
import com.floodrescue.shared.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RescueRequestController {

        private final RescueRequestService requestService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<RescueRequestResponse>> create(
                        @Valid @RequestPart("data") CreateRescueRequestDto dto,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images,
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Gửi yêu cầu thành công",
                                                requestService.create(dto, principal.getId(), images)));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<Page<RescueRequestResponse>>> getAll(
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String urgencyLevel,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
                        Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.success("OK",
                                requestService.getAll(status, urgencyLevel, fromDate, toDate, pageable)));
        }

        @GetMapping("/my")
        public ResponseEntity<ApiResponse<Page<RescueRequestResponse>>> getMy(
                        @AuthenticationPrincipal UserPrincipal principal,
                        Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.success("OK",
                                requestService.getMy(principal.getId(), pageable)));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<RescueRequestResponse>> getById(
                        @PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.success("OK",
                                requestService.getById(id)));
        }

        @PatchMapping("/{id}/verify")
        public ResponseEntity<ApiResponse<RescueRequestResponse>> verify(
                        @PathVariable Long id,
                        @RequestBody(required = false) VerifyRequestDto dto,
                        @AuthenticationPrincipal UserPrincipal principal) {
                if (dto == null)
                        dto = new VerifyRequestDto();
                return ResponseEntity.ok(ApiResponse.success("Xét duyệt thành công",
                                requestService.verify(id, dto, principal.getId())));
        }

        @PatchMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<RescueRequestResponse>> cancel(
                        @PathVariable Long id,
                        @Valid @RequestBody CancelRequestDto dto,
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.ok(ApiResponse.success("Hủy thành công",
                                requestService.cancel(id, dto, principal.getId(), principal.getRole())));
        }

        @PatchMapping("/{id}/confirm")
        public ResponseEntity<ApiResponse<RescueRequestResponse>> confirm(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.ok(ApiResponse.success("Xác nhận thành công",
                                requestService.confirm(id, principal.getId())));
        }
}