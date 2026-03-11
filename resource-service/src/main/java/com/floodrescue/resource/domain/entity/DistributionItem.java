package com.floodrescue.resource.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distribution_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribution_id", nullable = false)
    private Distribution distribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relief_item_id", nullable = false)
    private ReliefItem reliefItem;

    @Column(nullable = false)
    private Integer quantity;
}
