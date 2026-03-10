package com.floodrescue.request.domain.enums;

public enum RequestStatus {
    PENDING, // citizen vừa gửi, chờ coordinator xét duyệt
    VERIFIED, // coordinator đã duyệt, chờ assign team
    ASSIGNED, // đã assign team, team chưa xuất phát
    IN_PROGRESS, // team đang thực hiện
    COMPLETED, // team hoàn thành, chờ citizen xác nhận
    CONFIRMED, // citizen đã xác nhận xong
    CANCELLED // đã hủy (citizen hoặc coordinator hủy)
}
