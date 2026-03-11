package com.floodrescue.report.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_request_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRequestSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "total_requests")
    @Builder.Default
    private Integer totalRequests = 0;

    @Column(name = "critical_count")
    @Builder.Default
    private Integer criticalCount = 0;

    @Column(name = "high_count")
    @Builder.Default
    private Integer highCount = 0;

    @Column(name = "medium_count")
    @Builder.Default
    private Integer mediumCount = 0;

    @Column(name = "low_count")
    @Builder.Default
    private Integer lowCount = 0;

    @Column(name = "completed_count")
    @Builder.Default
    private Integer completedCount = 0;

    @Column(name = "cancelled_count")
    @Builder.Default
    private Integer cancelledCount = 0;

    // phút từ PENDING → ASSIGNED
    @Column(name = "avg_response_min", precision = 8, scale = 2)
    private BigDecimal avgResponseMin;

    // phút từ ASSIGNED → COMPLETED
    @Column(name = "avg_complete_min", precision = 8, scale = 2)
    private BigDecimal avgCompleteMin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
