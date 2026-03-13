import com.floodrescue.resource.domain.entity.Vehicle;
import com.floodrescue.resource.domain.entity.VehicleLog;
import com.floodrescue.resource.domain.enums.VehicleLogAction;
import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;
import com.floodrescue.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.resource.dto.response.VehicleResponse;
import com.floodrescue.resource.repository.VehicleLogRepository;
import com.floodrescue.resource.repository.VehicleRepository;
import com.floodrescue.resource.service.impl.VehicleServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleLogRepository vehicleLogRepository;

    @InjectMocks private VehicleServiceImpl vehicleService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private Vehicle buildVehicle(Long id, String plate, VehicleStatus status) {
        return Vehicle.builder()
                .id(id)
                .plateNumber(plate)
                .type(VehicleType.TRUCK)
                .capacity(10)
                .status(status)
                .build();
    }

    private CreateVehicleRequest buildCreateRequest(String plate) {
        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setPlateNumber(plate);
        req.setType(VehicleType.TRUCK);
        req.setCapacity(10);
        return req;
    }

    // =====================================================================
    // create()
    // =====================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create vehicle and return response when plate number is unique")
        void success() {
            when(vehicleRepository.existsByPlateNumber("51A-12345")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class)))
                    .thenReturn(buildVehicle(1L, "51A-12345", VehicleStatus.AVAILABLE));

            VehicleResponse response = vehicleService.create(buildCreateRequest("51A-12345"));

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPlateNumber()).isEqualTo("51A-12345");
            assertThat(response.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }

        @Test
        @DisplayName("should throw DUPLICATE_PLATE_NUMBER when plate already exists")
        void duplicatePlate_shouldThrow() {
            when(vehicleRepository.existsByPlateNumber("51A-12345")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.create(buildCreateRequest("51A-12345")))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PLATE_NUMBER);

            verify(vehicleRepository, never()).save(any());
        }
    }

    // =====================================================================
    // getAll()
    // =====================================================================

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("should call findAll when both status and type are null")
        void bothNull_shouldFindAll() {
            when(vehicleRepository.findAll())
                    .thenReturn(List.of(buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE)));

            List<VehicleResponse> responses = vehicleService.getAll(null, null);

            assertThat(responses).hasSize(1);
            verify(vehicleRepository).findAll();
            verify(vehicleRepository, never()).findByStatus(any());
            verify(vehicleRepository, never()).findByType(any());
            verify(vehicleRepository, never()).findByStatusAndType(any(), any());
        }

        @Test
        @DisplayName("should call findByStatus when only status is provided")
        void onlyStatus_shouldFindByStatus() {
            when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE))
                    .thenReturn(List.of(buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE)));

            List<VehicleResponse> responses = vehicleService.getAll(VehicleStatus.AVAILABLE, null);

            assertThat(responses).hasSize(1);
            verify(vehicleRepository).findByStatus(VehicleStatus.AVAILABLE);
            verify(vehicleRepository, never()).findAll();
            verify(vehicleRepository, never()).findByStatusAndType(any(), any());
        }

        @Test
        @DisplayName("should call findByType when only type is provided")
        void onlyType_shouldFindByType() {
            when(vehicleRepository.findByType(VehicleType.TRUCK))
                    .thenReturn(List.of(buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE)));

            List<VehicleResponse> responses = vehicleService.getAll(null, VehicleType.TRUCK);

            assertThat(responses).hasSize(1);
            verify(vehicleRepository).findByType(VehicleType.TRUCK);
            verify(vehicleRepository, never()).findAll();
            verify(vehicleRepository, never()).findByStatusAndType(any(), any());
        }

        @Test
        @DisplayName("should call findByStatusAndType when both status and type are provided")
        void bothProvided_shouldFindByStatusAndType() {
            when(vehicleRepository.findByStatusAndType(VehicleStatus.IN_USE, VehicleType.TRUCK))
                    .thenReturn(List.of(buildVehicle(1L, "51A-001", VehicleStatus.IN_USE)));

            List<VehicleResponse> responses = vehicleService.getAll(VehicleStatus.IN_USE, VehicleType.TRUCK);

            assertThat(responses).hasSize(1);
            verify(vehicleRepository).findByStatusAndType(VehicleStatus.IN_USE, VehicleType.TRUCK);
            verify(vehicleRepository, never()).findAll();
            verify(vehicleRepository, never()).findByStatus(any());
            verify(vehicleRepository, never()).findByType(any());
        }

        @Test
        @DisplayName("should return empty list when no vehicles match filters")
        void noMatchingVehicles_shouldReturnEmpty() {
            when(vehicleRepository.findByStatus(VehicleStatus.MAINTENANCE)).thenReturn(List.of());

            List<VehicleResponse> responses = vehicleService.getAll(VehicleStatus.MAINTENANCE, null);

            assertThat(responses).isEmpty();
        }
    }

    // =====================================================================
    // getById()
    // =====================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return vehicle response when vehicle exists")
        void success() {
            when(vehicleRepository.findById(1L))
                    .thenReturn(Optional.of(buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE)));

            VehicleResponse response = vehicleService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPlateNumber()).isEqualTo("51A-001");
        }

        @Test
        @DisplayName("should throw VEHICLE_NOT_FOUND when vehicle does not exist")
        void notFound_shouldThrow() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.getById(99L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VEHICLE_NOT_FOUND);
        }
    }

    // =====================================================================
    // updateStatus()
    // =====================================================================

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("should update vehicle status and save a vehicle log")
        void success() {
            Vehicle vehicle = buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VehicleResponse response = vehicleService.updateStatus(1L, VehicleStatus.IN_USE, "Assigned to team");

            assertThat(response.getStatus()).isEqualTo(VehicleStatus.IN_USE);
            verify(vehicleLogRepository).save(any(VehicleLog.class));
        }

        @Test
        @DisplayName("should map AVAILABLE status to RELEASED log action")
        void available_shouldLogReleased() {
            Vehicle vehicle = buildVehicle(1L, "51A-001", VehicleStatus.IN_USE);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            vehicleService.updateStatus(1L, VehicleStatus.AVAILABLE, "Released after mission");

            ArgumentCaptor<VehicleLog> logCaptor = ArgumentCaptor.forClass(VehicleLog.class);
            verify(vehicleLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getAction()).isEqualTo(VehicleLogAction.RELEASED);
        }

        @Test
        @DisplayName("should map IN_USE status to ASSIGNED log action")
        void inUse_shouldLogAssigned() {
            Vehicle vehicle = buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            vehicleService.updateStatus(1L, VehicleStatus.IN_USE, "Assigned");

            ArgumentCaptor<VehicleLog> logCaptor = ArgumentCaptor.forClass(VehicleLog.class);
            verify(vehicleLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getAction()).isEqualTo(VehicleLogAction.ASSIGNED);
        }

        @Test
        @DisplayName("should map MAINTENANCE status to MAINTENANCE log action")
        void maintenance_shouldLogMaintenance() {
            Vehicle vehicle = buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            vehicleService.updateStatus(1L, VehicleStatus.MAINTENANCE, "Scheduled maintenance");

            ArgumentCaptor<VehicleLog> logCaptor = ArgumentCaptor.forClass(VehicleLog.class);
            verify(vehicleLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getAction()).isEqualTo(VehicleLogAction.MAINTENANCE);
        }

        @Test
        @DisplayName("should map OFFLINE status to OFFLINE log action")
        void offline_shouldLogOffline() {
            Vehicle vehicle = buildVehicle(1L, "51A-001", VehicleStatus.AVAILABLE);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            vehicleService.updateStatus(1L, VehicleStatus.OFFLINE, "Vehicle decommissioned");

            ArgumentCaptor<VehicleLog> logCaptor = ArgumentCaptor.forClass(VehicleLog.class);
            verify(vehicleLogRepository).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getAction()).isEqualTo(VehicleLogAction.OFFLINE);
        }

        @Test
        @DisplayName("should throw VEHICLE_NOT_FOUND when vehicle does not exist")
        void notFound_shouldThrow() {
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.updateStatus(99L, VehicleStatus.AVAILABLE, "note"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VEHICLE_NOT_FOUND);

            verify(vehicleLogRepository, never()).save(any());
        }
    }
}