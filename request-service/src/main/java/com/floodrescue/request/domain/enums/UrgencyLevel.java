package com.floodrescue.request.domain.enums;

public enum UrgencyLevel {
    CRITICAL, // nguy hiểm tính mạng ngay lập tức
    HIGH, // nguy hiểm cao, cần xử lý trong 30 phút
    MEDIUM, // nguy hiểm trung bình, cần xử lý trong 2 giờ
    LOW // ảnh hưởng nhẹ, có thể chờ
}
