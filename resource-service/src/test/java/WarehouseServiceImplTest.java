import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.domain.entity.Warehouse;
import com.floodrescue.resource.dto.request.CreateWarehouseRequest;
import com.floodrescue.resource.dto.response.WarehouseResponse;
import com.floodrescue.resource.repository.WarehouseRepository;
import com.floodrescue.resource.service.impl.WarehouseServiceImpl;
import com.floodrescue.resource.shared.exception.AppException;
import com.floodrescue.resource.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock private WarehouseRepository warehouseRepository;

    @InjectMocks private WarehouseServiceImpl warehouseService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Warehouse buildWarehouse(Long id, String name) {
        return Warehouse.builder()
                .id(id)
                .name(name)
                .address("123 Flood St")
                .managerId(1L)
                .items(new ArrayList<>())
                .build();
    }

    private CreateWarehouseRequest buildCreateRequest(String name) {
        CreateWarehouseRequest req = new CreateWarehouseRequest();
        req.setName(name);
        req.setAddress("123 Flood St");
        return req;
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should save warehouse with correct fields and return response")
        void success() {
            Warehouse saved = buildWarehouse(1L, "Warehouse A");
            when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

            WarehouseResponse response = warehouseService.create(buildCreateRequest("Warehouse A"), 10L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Warehouse A");
            assertThat(response.getManagerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should assign managerId from parameter to warehouse")
        void shouldAssignManagerId() {
            when(warehouseRepository.save(any())).thenAnswer(inv -> {
                Warehouse w = inv.getArgument(0);
                w.setId(1L);
                return w;
            });

            warehouseService.create(buildCreateRequest("WH"), 99L);

            // Capture to verify managerId is correctly set from the parameter
            var captor = org.mockito.ArgumentCaptor.forClass(Warehouse.class);
            verify(warehouseRepository).save(captor.capture());
            assertThat(captor.getValue().getManagerId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("should return empty items list for a newly created warehouse")
        void newWarehouse_shouldHaveEmptyItemsList() {
            Warehouse saved = buildWarehouse(1L, "Warehouse A"); // items = empty list
            when(warehouseRepository.save(any())).thenReturn(saved);

            WarehouseResponse response = warehouseService.create(buildCreateRequest("Warehouse A"), 1L);

            assertThat(response.getItems()).isEmpty();
        }
    }

    // =====================================================================
    // getAll()
    // =====================================================================

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should return all warehouses with their items mapped")
        void success() {
            Warehouse wh1 = buildWarehouse(1L, "WH Alpha");
            Warehouse wh2 = buildWarehouse(2L, "WH Beta");
            when(warehouseRepository.findAllWithItems()).thenReturn(List.of(wh1, wh2));

            List<WarehouseResponse> responses = warehouseService.getAll();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("WH Alpha");
            assertThat(responses.get(1).getName()).isEqualTo("WH Beta");
        }

        @Test
        @DisplayName("should return empty list when no warehouses exist")
        void noWarehouses_shouldReturnEmpty() {
            when(warehouseRepository.findAllWithItems()).thenReturn(List.of());

            List<WarehouseResponse> responses = warehouseService.getAll();

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("should map relief items inside each warehouse to response")
        void shouldMapItemsInsideWarehouse() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            ReliefItem item = ReliefItem.builder()
                    .id(10L).warehouse(wh).name("Rice").unit("kg").quantity(100).lowThreshold(10).build();
            wh.getItems().add(item);
            when(warehouseRepository.findAllWithItems()).thenReturn(List.of(wh));

            List<WarehouseResponse> responses = warehouseService.getAll();

            assertThat(responses.getFirst().getItems()).hasSize(1);
            assertThat(responses.getFirst().getItems().getFirst().getName()).isEqualTo("Rice");
        }
    }

    // =====================================================================
    // getById()
    // =====================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return warehouse response when warehouse exists")
        void success() {
            Warehouse wh = buildWarehouse(1L, "WH Alpha");
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(wh));

            WarehouseResponse response = warehouseService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("WH Alpha");
        }

        @Test
        @DisplayName("should throw WAREHOUSE_NOT_FOUND when warehouse does not exist")
        void notFound_shouldThrow() {
            when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.getById(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAREHOUSE_NOT_FOUND);
        }
    }
}