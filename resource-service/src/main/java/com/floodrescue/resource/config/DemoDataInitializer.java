package com.floodrescue.resource.config;

import com.floodrescue.resource.domain.entity.Distribution;
import com.floodrescue.resource.domain.entity.DistributionItem;
import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.domain.entity.Vehicle;
import com.floodrescue.resource.domain.entity.Warehouse;
import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;
import com.floodrescue.resource.repository.DistributionRepository;
import com.floodrescue.resource.repository.ReliefItemRepository;
import com.floodrescue.resource.repository.VehicleRepository;
import com.floodrescue.resource.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final ReliefItemRepository reliefItemRepository;
    private final VehicleRepository vehicleRepository;
    private final DistributionRepository distributionRepository;

    @Override
    public void run(String... args) {
        seedWarehousesAndItems();
        seedVehicles();
        seedDistributionHistory();
    }

    private void seedWarehousesAndItems() {
        Map<String, Warehouse> warehousesByName = warehouseRepository.findAll().stream()
            .collect(Collectors.toMap(Warehouse::getName, warehouse -> warehouse));

        Warehouse central = warehousesByName.computeIfAbsent("Kho Trung tâm Q1", key -> warehouseRepository.save(Warehouse.builder()
            .name(key)
            .address("123 Đường XYZ, Quận 1, TP.HCM")
            .managerId(6L)
            .build()));

        Warehouse north = warehousesByName.computeIfAbsent("Kho Dự trữ Bình Thạnh", key -> warehouseRepository.save(Warehouse.builder()
            .name(key)
            .address("88 Đường Điện Biên Phủ, Bình Thạnh, TP.HCM")
            .managerId(6L)
            .build()));

        Set<String> existingItemKeys = reliefItemRepository.findAll().stream()
            .map(item -> item.getWarehouse().getId() + "::" + item.getName())
            .collect(Collectors.toSet());

        createItemIfMissing(central, "Áo phao", "Thiết bị an toàn", "cái", 120, 20, existingItemKeys);
        createItemIfMissing(central, "Mì gói", "Thực phẩm", "thùng", 350, 50, existingItemKeys);
        createItemIfMissing(central, "Nước uống đóng chai", "Nhu yếu phẩm", "thùng", 260, 40, existingItemKeys);
        createItemIfMissing(north, "Thuốc men cơ bản", "Y tế", "hộp", 80, 15, existingItemKeys);
        createItemIfMissing(north, "Chăn cứu trợ", "Dụng cụ sinh hoạt", "cái", 140, 25, existingItemKeys);

        log.info("Seeded demo warehouses and relief items");
    }

        private void createItemIfMissing(
            Warehouse warehouse,
            String name,
            String category,
            String unit,
            int quantity,
            int lowThreshold,
            Set<String> existingItemKeys) {
        String key = warehouse.getId() + "::" + name;
        if (existingItemKeys.contains(key)) {
            return;
        }

        reliefItemRepository.save(ReliefItem.builder()
                .warehouse(warehouse)
                .name(name)
                .category(category)
                .unit(unit)
                .quantity(quantity)
                .lowThreshold(lowThreshold)
                .build());
    }

    private void seedVehicles() {
        if (!vehicleRepository.existsByPlateNumber("51A-12345")) {
            vehicleRepository.save(Vehicle.builder()
                .plateNumber("51A-12345")
                .type(VehicleType.BOAT)
                .capacity(8)
                .status(VehicleStatus.AVAILABLE)
                .currentLat(new BigDecimal("10.77123400"))
                .currentLng(new BigDecimal("106.65890000"))
                .assignedTeamId(1L)
                .build());
        }

        if (!vehicleRepository.existsByPlateNumber("51B-67890")) {
            vehicleRepository.save(Vehicle.builder()
                .plateNumber("51B-67890")
                .type(VehicleType.TRUCK)
                .capacity(12)
                .status(VehicleStatus.IN_USE)
                .currentLat(new BigDecimal("10.76080000"))
                .currentLng(new BigDecimal("106.67010000"))
                .assignedTeamId(2L)
                .build());
        }

        if (!vehicleRepository.existsByPlateNumber("51C-24680")) {
            vehicleRepository.save(Vehicle.builder()
                .plateNumber("51C-24680")
                .type(VehicleType.AMBULANCE)
                .capacity(4)
                .status(VehicleStatus.MAINTENANCE)
                .currentLat(new BigDecimal("10.75230000"))
                .currentLng(new BigDecimal("106.68940000"))
                .build());
        }

        if (!vehicleRepository.existsByPlateNumber("51D-13579")) {
            vehicleRepository.save(Vehicle.builder()
                .plateNumber("51D-13579")
                .type(VehicleType.OTHER)
                .capacity(6)
                .status(VehicleStatus.AVAILABLE)
                .currentLat(new BigDecimal("10.74290000"))
                .currentLng(new BigDecimal("106.70320000"))
                .assignedTeamId(3L)
                .build());
        }

        log.info("Seeded demo vehicles");
    }

    private void seedDistributionHistory() {
        if (!distributionRepository.findAll().isEmpty()) {
            return;
        }

        List<ReliefItem> items = reliefItemRepository.findAll();
        if (items.isEmpty()) {
            return;
        }

        ReliefItem rice = items.stream().filter(item -> item.getName().contains("Mì gói")).findFirst().orElse(items.getFirst());
        ReliefItem water = items.stream().filter(item -> item.getName().contains("Nước uống")).findFirst().orElse(items.get(0));

        Distribution distribution = Distribution.builder()
                .requestId(1L)
                .recipientId(1L)
                .coordinatorId(5L)
                .note("Phân phối demo cho hộ dân vùng ngập nặng")
                .items(new ArrayList<>())
                .build();

        DistributionItem item1 = DistributionItem.builder()
                .distribution(distribution)
                .reliefItem(rice)
                .quantity(2)
                .build();

        DistributionItem item2 = DistributionItem.builder()
                .distribution(distribution)
                .reliefItem(water)
                .quantity(1)
                .build();

        distribution.getItems().add(item1);
        distribution.getItems().add(item2);

        distributionRepository.save(distribution);

        log.info("Seeded demo distribution history");
    }
}