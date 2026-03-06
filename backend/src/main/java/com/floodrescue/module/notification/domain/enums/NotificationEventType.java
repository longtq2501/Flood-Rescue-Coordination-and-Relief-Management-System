package com.floodrescue.module.notification.domain.enums;

public enum NotificationEventType {
    NEW_REQUEST_ALERT, // có yêu cầu cứu hộ mới → gửi đến COORDINATOR
    REQUEST_ASSIGNED, // request đã được assign → gửi đến CITIZEN
    REQUEST_COMPLETED, // request hoàn thành → gửi đến CITIZEN
    REQUEST_STATUS, // cập nhật status → gửi đến CITIZEN
    RESOURCE_LOW_ALERT, // tồn kho thấp → gửi đến MANAGER
    SYSTEM_BROADCAST // thông báo hệ thống → gửi đến tất cả
}