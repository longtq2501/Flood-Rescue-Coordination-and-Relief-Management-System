import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.domain.entity.Warehouse;
import com.floodrescue.resource.dto.request.CreateReliefItemRequest;
import com.floodrescue.resource.dto.request.UpdateStockRequest;
import com.floodrescue.resource.dto.response.ReliefItemResponse;
import com.floodrescue.resource.repository.ReliefItemRepository;
import com.floodrescue.resource.repository.WarehouseRepository;
import com.floodrescue.resource.service.impl.InventoryServiceImpl;
import com.floodrescue.resource.shared.exception.AppException;
import com.floodrescue.resource.shared.exception.ErrorCode;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private ReliefItemRepository reliefItemRepository;
    @Mock private WarehouseRepository warehouseRepository;

    @InjectMocks private InventoryServiceImpl inventoryService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Warehouse buildWarehouse(Long id) {
        return Warehouse.builder().id(id).name("WH Alpha").managerId(1L).build();
    }

    private ReliefItem buildItem(Long id, Warehouse warehouse, int quantity) {
        return ReliefItem.builder()
                .id(id)
                .warehouse(warehouse)
                .name("Rice")
                .category("Food")
                .unit("kg")
                .quantity(quantity)
                .lowThreshold(10)
                .build();
    }

    private CreateReliefItemRequest buildCreateRequest(Long warehouseId, int quantity) {
        CreateReliefItemRequest req = new CreateReliefItemRequest();
        req.setWarehouseId(warehouseId);
        req.setName("Rice");
        req.setCategory("Food");
        req.setUnit("kg");
        req.setQuantity(quantity);
        req.setLowThreshold(10);
        return req;
    }

    // =====================================================================
    // addItem()
    // =====================================================================

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("should create and return item when warehouse exists")
        void success() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem saved = buildItem(10L, wh, 100);
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));
            when(reliefItemRepository.save(any(ReliefItem.class))).thenReturn(saved);

            ReliefItemResponse response = inventoryService.addItem(buildCreateRequest(1L, 100));

            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getName()).isEqualTo("Rice");
            assertThat(response.getQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("should throw WAREHOUSE_NOT_FOUND when warehouse does not exist")
        void warehouseNotFound_shouldThrow() {
            when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.addItem(buildCreateRequest(99L, 50)))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAREHOUSE_NOT_FOUND);

            verify(reliefItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("should save item linked to the correct warehouse")
        void shouldLinkItemToWarehouse() {
            Warehouse wh = buildWarehouse(1L);
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));
            when(reliefItemRepository.save(any())).thenAnswer(inv -> {
                ReliefItem item = inv.getArgument(0);
                item.setId(1L);
                return item;
            });

            inventoryService.addItem(buildCreateRequest(1L, 50));

            ArgumentCaptor<ReliefItem> captor = ArgumentCaptor.forClass(ReliefItem.class);
            verify(reliefItemRepository).save(captor.capture());
            assertThat(captor.getValue().getWarehouse().getId()).isEqualTo(1L);
        }
    }

    // =====================================================================
    // getItemsByWarehouse()
    // =====================================================================

    @Nested
    @DisplayName("getItemsByWarehouse()")
    class GetItemsByWarehouse {

        @Test
        @DisplayName("should return paged items for a warehouse")
        void success() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(10L, wh, 100);
            Pageable pageable = PageRequest.of(0, 10);
            when(reliefItemRepository.findByWarehouseId(eq(1L), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

            Page<ReliefItemResponse> result = inventoryService.getItemsByWarehouse(1L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getName()).isEqualTo("Rice");
        }

        @Test
        @DisplayName("should return empty page when warehouse has no items")
        void noItems_shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(reliefItemRepository.findByWarehouseId(eq(1L), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Page<ReliefItemResponse> result = inventoryService.getItemsByWarehouse(1L, pageable);

            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =====================================================================
    // updateStock()
    // =====================================================================

    @Nested
    @DisplayName("updateStock()")
    class UpdateStock {

        @Test
        @DisplayName("should increase quantity when delta is positive (restocking)")
        void positiveQuantity_shouldIncrease() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(1L, wh, 50);
            when(reliefItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(reliefItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(30); // restock +30

            ReliefItemResponse response = inventoryService.updateStock(1L, req);

            assertThat(response.getQuantity()).isEqualTo(80); // 50 + 30
        }

        @Test
        @DisplayName("should decrease quantity when delta is negative (consumption)")
        void negativeQuantity_shouldDecrease() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(1L, wh, 50);
            when(reliefItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(reliefItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(-20); // consume -20

            ReliefItemResponse response = inventoryService.updateStock(1L, req);

            assertThat(response.getQuantity()).isEqualTo(30); // 50 - 20
        }

        @Test
        @DisplayName("should throw VALIDATION_ERROR when quantity delta is zero")
        void zeroDelta_shouldThrow() {
            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(0);

            assertThatThrownBy(() -> inventoryService.updateStock(1L, req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

            // Zero guard must fire BEFORE fetching from DB
            verify(reliefItemRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should throw INSUFFICIENT_STOCK when resulting quantity would be negative")
        void insufficientStock_shouldThrow() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(1L, wh, 10); // only 10 in stock
            when(reliefItemRepository.findById(1L)).thenReturn(Optional.of(item));

            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(-20); // trying to consume 20, but only 10 available

            assertThatThrownBy(() -> inventoryService.updateStock(1L, req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);

            verify(reliefItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ITEM_NOT_FOUND when item does not exist")
        void itemNotFound_shouldThrow() {
            when(reliefItemRepository.findById(99L)).thenReturn(Optional.empty());

            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(10);

            assertThatThrownBy(() -> inventoryService.updateStock(99L, req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("should allow reducing stock to exactly zero (boundary case)")
        void reduceToExactlyZero_shouldSucceed() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(1L, wh, 20);
            when(reliefItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(reliefItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateStockRequest req = new UpdateStockRequest();
            req.setQuantity(-20); // exact depletion

            ReliefItemResponse response = inventoryService.updateStock(1L, req);

            assertThat(response.getQuantity()).isEqualTo(0);
        }
    }

    // =====================================================================
    // getById()
    // =====================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return item response when item exists")
        void success() {
            Warehouse wh = buildWarehouse(1L);
            ReliefItem item = buildItem(10L, wh, 50);
            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item));

            ReliefItemResponse response = inventoryService.getById(10L);

            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("should throw ITEM_NOT_FOUND when item does not exist")
        void notFound_shouldThrow() {
            when(reliefItemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getById(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
        }
    }
}