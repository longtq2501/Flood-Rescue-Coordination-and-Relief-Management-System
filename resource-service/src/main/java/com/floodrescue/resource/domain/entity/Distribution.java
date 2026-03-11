package com.floodrescue.resource.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "coordinator_id", nullable = false)
    private Long coordinatorId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "distributed_at", nullable = false, updatable = false)
    private LocalDateTime distributedAt;

    @OneToMany(mappedBy = "distribution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DistributionItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        distributedAt = LocalDateTime.now();
    }
}
