import com.floodrescue.resource.domain.entity.Distribution;
import com.floodrescue.resource.domain.entity.DistributionItem;
import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.domain.entity.Warehouse;
import com.floodrescue.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.resource.dto.response.DistributionResponse;
import com.floodrescue.resource.event.ResourceDistributedEvent;
import com.floodrescue.resource.event.ResourceEventPublisher;
import com.floodrescue.resource.event.ResourceStockLowEvent;
import com.floodrescue.resource.repository.DistributionRepository;
import com.floodrescue.resource.repository.ReliefItemRepository;
import com.floodrescue.resource.service.impl.DistributionServiceImpl;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionServiceImplTest {

    @Mock private DistributionRepository distributionRepository;
    @Mock private ReliefItemRepository reliefItemRepository;
    @Mock private ResourceEventPublisher eventPublisher;

    @InjectMocks private DistributionServiceImpl distributionService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Warehouse buildWarehouse(Long id, String name) {
        return Warehouse.builder().id(id).name(name).managerId(1L).build();
    }

    /**
     * Builds a ReliefItem with isBelowThreshold() returning false by default:
     * quantity=100, lowThreshold=10 → 100 > 10 → not below threshold
     */
    private ReliefItem buildItem(Long id, Warehouse warehouse, int quantity, int lowThreshold) {
        return ReliefItem.builder()
                .id(id)
                .warehouse(warehouse)
                .name("Rice")
                .unit("kg")
                .quantity(quantity)
                .lowThreshold(lowThreshold)
                .build();
    }

    private CreateDistributionRequest buildRequest(List<CreateDistributionRequest.DistributionItemRequest> items) {
        CreateDistributionRequest req = new CreateDistributionRequest();
        req.setRequestId(100L);
        req.setRecipientId(50L);
        req.setNote("Flood relief");
        req.setItems(items);
        return req;
    }

    private CreateDistributionRequest.DistributionItemRequest buildItemRequest(Long reliefItemId, int qty) {
        CreateDistributionRequest.DistributionItemRequest item =
                new CreateDistributionRequest.DistributionItemRequest();
        item.setReliefItemId(reliefItemId);
        item.setQuantity(qty);
        return item;
    }

    private Distribution buildSavedDistribution(Long id) {
        Distribution d = Distribution.builder()
                .id(id)
                .requestId(100L)
                .recipientId(50L)
                .coordinatorId(1L)
                .note("Flood relief")
                .distributedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
        return d;
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should deduct stock, save distribution, and publish distributed event")
        void success_singleItem() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            ReliefItem item = buildItem(10L, wh, 100, 5); // 100 qty, threshold 5 → not below

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(distributionRepository.save(any(Distribution.class)))
                    .thenAnswer(inv -> {
                        Distribution d = inv.getArgument(0);
                        d.setId(1L);
                        d.setDistributedAt(LocalDateTime.now());
                        return d;
                    });
            when(reliefItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            CreateDistributionRequest request = buildRequest(List.of(buildItemRequest(10L, 30)));

            DistributionResponse response = distributionService.create(request, 1L);

            // ASSERT — stock must be deducted
            assertThat(item.getQuantity()).isEqualTo(70); // 100 - 30

            // ASSERT — distributed event must be published
            verify(eventPublisher).publishDistributed(any(ResourceDistributedEvent.class));
            // ASSERT — no stock-low event since 70 > threshold 5
            verify(eventPublisher, never()).publishStockLow(any());
        }

        @Test
        @DisplayName("should publish stock-low event for each item that falls below threshold after deduction")
        void belowThreshold_shouldPublishStockLowEvent() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            // After deducting 95, quantity becomes 5 which equals lowThreshold=5 → isBelowThreshold=true
            ReliefItem item = buildItem(10L, wh, 100, 5);

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(distributionRepository.save(any())).thenAnswer(inv -> {
                Distribution d = inv.getArgument(0);
                d.setId(1L);
                d.setDistributedAt(LocalDateTime.now());
                return d;
            });
            when(reliefItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            CreateDistributionRequest request = buildRequest(List.of(buildItemRequest(10L, 95)));

            distributionService.create(request, 1L);

            // quantity = 100 - 95 = 5, threshold = 5 → isBelowThreshold() = true
            ArgumentCaptor<ResourceStockLowEvent> stockLowCaptor =
                    ArgumentCaptor.forClass(ResourceStockLowEvent.class);
            verify(eventPublisher).publishStockLow(stockLowCaptor.capture());
            assertThat(stockLowCaptor.getValue().getItemId()).isEqualTo(10L);
            assertThat(stockLowCaptor.getValue().getCurrentQuantity()).isEqualTo(5);
            assertThat(stockLowCaptor.getValue().getThreshold()).isEqualTo(5);
        }

        @Test
        @DisplayName("should publish distributed event with null warehouseId when items span multiple warehouses")
        void multipleWarehouses_shouldPublishWithNullWarehouseId() {
            Warehouse wh1 = buildWarehouse(1L, "WH Alpha");
            Warehouse wh2 = buildWarehouse(2L, "WH Beta");
            ReliefItem item1 = buildItem(10L, wh1, 100, 5);
            ReliefItem item2 = buildItem(11L, wh2, 50, 5);

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item1));
            when(reliefItemRepository.findById(11L)).thenReturn(Optional.of(item2));
            when(distributionRepository.save(any())).thenAnswer(inv -> {
                Distribution d = inv.getArgument(0);
                d.setId(1L);
                d.setDistributedAt(LocalDateTime.now());
                return d;
            });
            when(reliefItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            CreateDistributionRequest request = buildRequest(List.of(
                    buildItemRequest(10L, 10),
                    buildItemRequest(11L, 5)
            ));

            distributionService.create(request, 1L);

            // warehouseId must be null when items span more than one warehouse
            ArgumentCaptor<ResourceDistributedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ResourceDistributedEvent.class);
            verify(eventPublisher).publishDistributed(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getWarehouseId()).isNull();
            assertThat(eventCaptor.getValue().getTotalItems()).isEqualTo(2);
        }

        @Test
        @DisplayName("should publish distributed event with correct warehouseId when all items from same warehouse")
        void singleWarehouse_shouldPublishWithWarehouseId() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            ReliefItem item1 = buildItem(10L, wh, 100, 5);
            ReliefItem item2 = buildItem(11L, wh, 50, 5); // same warehouse

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item1));
            when(reliefItemRepository.findById(11L)).thenReturn(Optional.of(item2));
            when(distributionRepository.save(any())).thenAnswer(inv -> {
                Distribution d = inv.getArgument(0);
                d.setId(1L);
                d.setDistributedAt(LocalDateTime.now());
                return d;
            });
            when(reliefItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            CreateDistributionRequest request = buildRequest(List.of(
                    buildItemRequest(10L, 10),
                    buildItemRequest(11L, 5)
            ));

            distributionService.create(request, 1L);

            ArgumentCaptor<ResourceDistributedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ResourceDistributedEvent.class);
            verify(eventPublisher).publishDistributed(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getWarehouseId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw ITEM_NOT_FOUND when a relief item does not exist")
        void itemNotFound_shouldThrow() {
            when(reliefItemRepository.findById(99L)).thenReturn(Optional.empty());

            CreateDistributionRequest request = buildRequest(List.of(buildItemRequest(99L, 10)));

            assertThatThrownBy(() -> distributionService.create(request, 1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);

            verify(distributionRepository, never()).save(any());
            verify(eventPublisher, never()).publishDistributed(any());
        }

        @Test
        @DisplayName("should throw INSUFFICIENT_STOCK when requested quantity exceeds available stock")
        void insufficientStock_shouldThrow() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            ReliefItem item = buildItem(10L, wh, 20, 5); // only 20 available

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item));

            CreateDistributionRequest request = buildRequest(List.of(buildItemRequest(10L, 50))); // want 50

            assertThatThrownBy(() -> distributionService.create(request, 1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);

            // Stock must NOT be modified on failure
            assertThat(item.getQuantity()).isEqualTo(20);
            verify(distributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw INSUFFICIENT_STOCK at the failing item without processing subsequent items")
        void insufficientStock_onSecondItem_shouldNotProcessFurtherItems() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            ReliefItem item1 = buildItem(10L, wh, 100, 5); // OK
            ReliefItem item2 = buildItem(11L, wh, 5, 5);   // insufficient

            when(reliefItemRepository.findById(10L)).thenReturn(Optional.of(item1));
            when(reliefItemRepository.findById(11L)).thenReturn(Optional.of(item2));

            CreateDistributionRequest request = buildRequest(List.of(
                    buildItemRequest(10L, 10),  // would succeed
                    buildItemRequest(11L, 50)   // would fail
            ));

            assertThatThrownBy(() -> distributionService.create(request, 1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);

            verify(distributionRepository, never()).save(any());
            verify(reliefItemRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("should not publish any event when item list is empty")
        void emptyItems_shouldNotPublishEvent() {
            // An empty items list means firstItem stays null → no event published
            CreateDistributionRequest request = buildRequest(List.of());
            when(distributionRepository.save(any())).thenAnswer(inv -> {
                Distribution d = inv.getArgument(0);
                d.setId(1L);
                return d;
            });
            when(reliefItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            distributionService.create(request, 1L);

            verify(eventPublisher, never()).publishDistributed(any());
            verify(eventPublisher, never()).publishStockLow(any());
        }
    }

    // =====================================================================
    // getAll()
    // =====================================================================

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should return paged distributions enriched with items")
        void success() {
            Pageable pageable = PageRequest.of(0, 10);
            Distribution d = buildSavedDistribution(1L);
            Page<Distribution> page = new PageImpl<>(List.of(d), pageable, 1);

            when(distributionRepository.findAll(pageable)).thenReturn(page);
            // fetchWithItems returns the same list enriched — simulate same data
            when(distributionRepository.fetchWithItems(anyList())).thenReturn(List.of(d));

            Page<DistributionResponse> result = distributionService.getAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
            // fetchWithItems must be called to eagerly load items and avoid N+1
            verify(distributionRepository).fetchWithItems(List.of(d));
        }

        @Test
        @DisplayName("should return empty page when no distributions exist")
        void empty_shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(distributionRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));
            when(distributionRepository.fetchWithItems(anyList())).thenReturn(List.of());

            Page<DistributionResponse> result = distributionService.getAll(pageable);

            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =====================================================================
    // getByRequestId()
    // =====================================================================

    @Nested
    @DisplayName("getByRequestId()")
    class GetByRequestId {

        @Test
        @DisplayName("should return paged distributions for a specific requestId")
        void success() {
            Pageable pageable = PageRequest.of(0, 10);
            Distribution d = buildSavedDistribution(1L);
            Page<Distribution> page = new PageImpl<>(List.of(d), pageable, 1);

            when(distributionRepository.findByRequestId(100L, pageable)).thenReturn(page);
            when(distributionRepository.fetchWithItems(anyList())).thenReturn(List.of(d));

            Page<DistributionResponse> result = distributionService.getByRequestId(100L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getRequestId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("should return empty page when no distributions exist for requestId")
        void noDistributions_shouldReturnEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(distributionRepository.findByRequestId(999L, pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));
            when(distributionRepository.fetchWithItems(anyList())).thenReturn(List.of());

            Page<DistributionResponse> result = distributionService.getByRequestId(999L, pageable);

            assertThat(result.getTotalElements()).isZero();
        }
    }
}