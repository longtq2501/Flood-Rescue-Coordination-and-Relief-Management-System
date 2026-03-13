import com.floodrescue.report.domain.entity.DailyRequestSnapshot;
import com.floodrescue.report.domain.entity.DailyResourceSnapshot;
import com.floodrescue.report.domain.entity.TeamPerformanceSnapshot;
import com.floodrescue.report.dto.response.DashboardResponse;
import com.floodrescue.report.repository.DailyRequestSnapshotRepository;
import com.floodrescue.report.repository.DailyResourceSnapshotRepository;
import com.floodrescue.report.repository.TeamPerformanceSnapshotRepository;
import com.floodrescue.report.service.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private DailyRequestSnapshotRepository requestSnapshotRepo;
    @Mock private DailyResourceSnapshotRepository resourceSnapshotRepo;
    @Mock private TeamPerformanceSnapshotRepository teamPerformanceRepo;

    @InjectMocks private ReportServiceImpl reportService;

    // =====================================================================
    // HELPERS
    // =====================================================================

    private DailyRequestSnapshot buildRequestSnapshot(LocalDate date, int completedCount,
                                                      BigDecimal avgCompleteMin) {
        return DailyRequestSnapshot.builder()
                .id(1L)
                .snapshotDate(date)
                .totalRequests(10)
                .criticalCount(2)
                .completedCount(completedCount)
                .cancelledCount(1)
                .avgResponseMin(BigDecimal.valueOf(5.00))
                .avgCompleteMin(avgCompleteMin)
                .build();
    }

    private TeamPerformanceSnapshot buildTeamSnapshot(LocalDate date, Long teamId,
                                                      int missions, BigDecimal avgDuration) {
        return TeamPerformanceSnapshot.builder()
                .id(1L)
                .snapshotDate(date)
                .teamId(teamId)
                .missionsCompleted(missions)
                .avgDurationMin(avgDuration)
                .build();
    }

    // =====================================================================
    // getDashboard()
    // =====================================================================

    @Nested
    @DisplayName("getDashboard()")
    class GetDashboard {

        @Test
        @DisplayName("should return full dashboard with today summary, chart and team ranking")
        void success_withAllData() {
            LocalDate today = LocalDate.now();

            // Today's request snapshot
            DailyRequestSnapshot todaySnapshot = buildRequestSnapshot(today, 5, BigDecimal.valueOf(30.00));
            when(requestSnapshotRepo.findBySnapshotDate(today)).thenReturn(Optional.of(todaySnapshot));

            // Today's resource snapshots (2 warehouses)
            DailyResourceSnapshot res1 = DailyResourceSnapshot.builder()
                    .snapshotDate(today).warehouseId(1L).totalDistributions(10).build();
            DailyResourceSnapshot res2 = DailyResourceSnapshot.builder()
                    .snapshotDate(today).warehouseId(2L).totalDistributions(5).build();
            when(resourceSnapshotRepo.findBySnapshotDate(today)).thenReturn(List.of(res1, res2));

            // Last 30 days chart data
            DailyRequestSnapshot chartSnapshot = buildRequestSnapshot(today.minusDays(1), 3, null);
            when(requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc())
                    .thenReturn(List.of(todaySnapshot, chartSnapshot));

            // Team ranking last 7 days
            TeamPerformanceSnapshot perf = buildTeamSnapshot(today, 1L, 8, BigDecimal.valueOf(45.00));
            when(teamPerformanceRepo.findBySnapshotDateBetweenOrderByMissionsCompletedDesc(any(), any()))
                    .thenReturn(List.of(perf));

            // ACT
            DashboardResponse response = reportService.getDashboard();

            // ASSERT — today summary
            assertThat(response.getToday().getCompletedCount()).isEqualTo(5);
            assertThat(response.getToday().getTotalDistributions()).isEqualTo(15); // 10 + 5
            assertThat(response.getToday().getTeamsActive()).isEqualTo(0); // TODO API call

            // ASSERT — chart data
            assertThat(response.getRequestChart()).hasSize(2);
            assertThat(response.getRequestChart().getFirst().getTotalRequests()).isEqualTo(10);

            // ASSERT — team ranking
            assertThat(response.getTeamRanking()).hasSize(1);
            assertThat(response.getTeamRanking().getFirst().getTeamId()).isEqualTo(1L);
            assertThat(response.getTeamRanking().getFirst().getMissionsCompleted()).isEqualTo(8);
        }

        @Test
        @DisplayName("should return zero-value summary when no snapshot exists for today")
        void noTodaySnapshot_shouldReturnZeroSummary() {
            LocalDate today = LocalDate.now();
            when(requestSnapshotRepo.findBySnapshotDate(today)).thenReturn(Optional.empty());
            when(requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc()).thenReturn(List.of());
            when(teamPerformanceRepo.findBySnapshotDateBetweenOrderByMissionsCompletedDesc(any(), any()))
                    .thenReturn(List.of());

            // ACT
            DashboardResponse response = reportService.getDashboard();

            // ASSERT — all fields must be 0 / zero, not null (safe for frontend rendering)
            assertThat(response.getToday().getTotalRequests()).isEqualTo(0);
            assertThat(response.getToday().getCompletedCount()).isEqualTo(0);
            assertThat(response.getToday().getAvgCompleteMin()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getToday().getTotalDistributions()).isEqualTo(0);

            // resourceSnapshotRepo must NOT be called when request snapshot is absent
            verify(resourceSnapshotRepo, never()).findBySnapshotDate(any());
        }

        @Test
        @DisplayName("should return empty chart and ranking when no historical data exists")
        void noHistoricalData_shouldReturnEmptyLists() {
            LocalDate today = LocalDate.now();
            when(requestSnapshotRepo.findBySnapshotDate(today)).thenReturn(Optional.empty());
            when(requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc()).thenReturn(List.of());
            when(teamPerformanceRepo.findBySnapshotDateBetweenOrderByMissionsCompletedDesc(any(), any()))
                    .thenReturn(List.of());

            DashboardResponse response = reportService.getDashboard();

            assertThat(response.getRequestChart()).isEmpty();
            assertThat(response.getTeamRanking()).isEmpty();
        }

        @Test
        @DisplayName("should sum totalDistributions across all warehouses for today")
        void totalDistributions_shouldSumAllWarehouses() {
            LocalDate today = LocalDate.now();
            DailyRequestSnapshot todaySnapshot = buildRequestSnapshot(today, 3, null);
            when(requestSnapshotRepo.findBySnapshotDate(today)).thenReturn(Optional.of(todaySnapshot));

            // 3 warehouses with different distribution counts
            when(resourceSnapshotRepo.findBySnapshotDate(today)).thenReturn(List.of(
                    DailyResourceSnapshot.builder().warehouseId(1L).totalDistributions(100).build(),
                    DailyResourceSnapshot.builder().warehouseId(2L).totalDistributions(50).build(),
                    DailyResourceSnapshot.builder().warehouseId(3L).totalDistributions(25).build()
            ));
            when(requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc()).thenReturn(List.of());
            when(teamPerformanceRepo.findBySnapshotDateBetweenOrderByMissionsCompletedDesc(any(), any()))
                    .thenReturn(List.of());

            DashboardResponse response = reportService.getDashboard();

            assertThat(response.getToday().getTotalDistributions()).isEqualTo(175); // 100+50+25
        }

        @Test
        @DisplayName("should query team ranking with correct 7-day date range")
        void teamRanking_shouldUseCorrect7DayRange() {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);

            when(requestSnapshotRepo.findBySnapshotDate(today)).thenReturn(Optional.empty());
            when(requestSnapshotRepo.findTop30ByOrderBySnapshotDateDesc()).thenReturn(List.of());
            when(teamPerformanceRepo.findBySnapshotDateBetweenOrderByMissionsCompletedDesc(any(), any()))
                    .thenReturn(List.of());

            reportService.getDashboard();

            // VERIFY the exact date range passed into the repository
            verify(teamPerformanceRepo).findBySnapshotDateBetweenOrderByMissionsCompletedDesc(weekAgo, today);
        }
    }

    // =====================================================================
    // runDailyEtl()
    // =====================================================================

    @Nested
    @DisplayName("runDailyEtl()")
    class RunDailyEtl {

        @Test
        @DisplayName("should do nothing when snapshot already exists for the date")
        void snapshotAlreadyExists_shouldNotCreateNew() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot existing = buildRequestSnapshot(date, 5, null);
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(existing));

            reportService.runDailyEtl(date);

            // Snapshot already exists — save() must NOT be called
            verify(requestSnapshotRepo, never()).save(any());
        }

        @Test
        @DisplayName("should create empty snapshot when none exists for the date")
        void noSnapshot_shouldCreateEmptyOne() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.empty());
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.runDailyEtl(date);

            // VERIFY the saved snapshot has the correct date and zero defaults
            ArgumentCaptor<DailyRequestSnapshot> captor =
                    ArgumentCaptor.forClass(DailyRequestSnapshot.class);
            verify(requestSnapshotRepo).save(captor.capture());
            assertThat(captor.getValue().getSnapshotDate()).isEqualTo(date);
            assertThat(captor.getValue().getCompletedCount()).isEqualTo(0);
            assertThat(captor.getValue().getTotalRequests()).isEqualTo(0);
        }
    }

    // =====================================================================
    // updateRequestSnapshot()
    // =====================================================================

    @Nested
    @DisplayName("updateRequestSnapshot()")
    class UpdateRequestSnapshot {

        @Test
        @DisplayName("should increment completedCount and calculate avgCompleteMin on first completion")
        void firstCompletion_shouldSetAvgToFirstDuration() {
            // ARRANGE — brand new snapshot, completedCount=0, avgCompleteMin=null
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot freshSnapshot = buildRequestSnapshot(date, 0, null);
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(freshSnapshot));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT — first completion with 60 minutes duration
            reportService.updateRequestSnapshot(date, null, 60);

            // ASSERT
            ArgumentCaptor<DailyRequestSnapshot> captor =
                    ArgumentCaptor.forClass(DailyRequestSnapshot.class);
            verify(requestSnapshotRepo).save(captor.capture());

            DailyRequestSnapshot saved = captor.getValue();
            assertThat(saved.getCompletedCount()).isEqualTo(1);
            // First completion: avg = (0 * 0 + 60) / 1 = 60.00
            assertThat(saved.getAvgCompleteMin()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        }

        @Test
        @DisplayName("should recalculate running average correctly on subsequent completions")
        void subsequentCompletion_shouldRecalculateRunningAverage() {
            // ARRANGE — snapshot already has 2 completions with avg of 40.00 min
            // Formula: newAvg = (oldAvg * oldCount + newDuration) / newCount
            // Expected: (40.00 * 2 + 20) / 3 = 100 / 3 = 33.33
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot snapshot = buildRequestSnapshot(date, 2, BigDecimal.valueOf(40.00));
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(snapshot));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT — 3rd completion with 20 minutes
            reportService.updateRequestSnapshot(date, null, 20);

            ArgumentCaptor<DailyRequestSnapshot> captor =
                    ArgumentCaptor.forClass(DailyRequestSnapshot.class);
            verify(requestSnapshotRepo).save(captor.capture());

            DailyRequestSnapshot saved = captor.getValue();
            assertThat(saved.getCompletedCount()).isEqualTo(3);
            assertThat(saved.getAvgCompleteMin()).isEqualByComparingTo(new BigDecimal("33.33"));
        }

        @Test
        @DisplayName("should increment completedCount but not update avgCompleteMin when duration is null")
        void nullDuration_shouldIncrementCountOnly() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot snapshot = buildRequestSnapshot(date, 3, BigDecimal.valueOf(50.00));
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(snapshot));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT — duration unknown (null)
            reportService.updateRequestSnapshot(date, null, null);

            ArgumentCaptor<DailyRequestSnapshot> captor =
                    ArgumentCaptor.forClass(DailyRequestSnapshot.class);
            verify(requestSnapshotRepo).save(captor.capture());

            DailyRequestSnapshot saved = captor.getValue();
            assertThat(saved.getCompletedCount()).isEqualTo(4);
            // avgCompleteMin must remain unchanged when duration is null
            assertThat(saved.getAvgCompleteMin()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("should create new snapshot and calculate avg when no snapshot exists for date")
        void noExistingSnapshot_shouldCreateAndCalculate() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            // orElseGet creates a fresh snapshot with @Builder.Default completedCount=0
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.empty());
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.updateRequestSnapshot(date, null, 45);

            ArgumentCaptor<DailyRequestSnapshot> captor =
                    ArgumentCaptor.forClass(DailyRequestSnapshot.class);
            verify(requestSnapshotRepo).save(captor.capture());

            DailyRequestSnapshot saved = captor.getValue();
            assertThat(saved.getSnapshotDate()).isEqualTo(date);
            assertThat(saved.getCompletedCount()).isEqualTo(1);
            assertThat(saved.getAvgCompleteMin()).isEqualByComparingTo(new BigDecimal("45.00"));
        }

        @Test
        @DisplayName("should also update TeamPerformanceSnapshot when teamId is provided")
        void withTeamId_shouldUpdateTeamPerformanceToo() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot snapshot = buildRequestSnapshot(date, 1, BigDecimal.valueOf(30.00));
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(snapshot));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Team performance — fresh snapshot
            when(teamPerformanceRepo.findBySnapshotDateAndTeamId(date, 1L))
                    .thenReturn(Optional.empty());
            when(teamPerformanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT
            reportService.updateRequestSnapshot(date, 1L, 60);

            // VERIFY both repositories are saved
            verify(requestSnapshotRepo).save(any(DailyRequestSnapshot.class));
            verify(teamPerformanceRepo).save(any(TeamPerformanceSnapshot.class));
        }

        @Test
        @DisplayName("should NOT update TeamPerformanceSnapshot when teamId is null")
        void nullTeamId_shouldSkipTeamPerformanceUpdate() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyRequestSnapshot snapshot = buildRequestSnapshot(date, 0, null);
            when(requestSnapshotRepo.findBySnapshotDate(date)).thenReturn(Optional.of(snapshot));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.updateRequestSnapshot(date, null, 30);

            // teamPerformanceRepo must never be touched
            verify(teamPerformanceRepo, never()).findBySnapshotDateAndTeamId(any(), any());
            verify(teamPerformanceRepo, never()).save(any());
        }
    }

    // =====================================================================
    // updateResourceSnapshot()
    // =====================================================================

    @Nested
    @DisplayName("updateResourceSnapshot()")
    class UpdateResourceSnapshot {

        @Test
        @DisplayName("should increment totalDistributions when snapshot exists for date and warehouse")
        void existingSnapshot_shouldIncrement() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            DailyResourceSnapshot existing = DailyResourceSnapshot.builder()
                    .snapshotDate(date).warehouseId(1L).totalDistributions(5).build();
            when(resourceSnapshotRepo.findBySnapshotDateAndWarehouseId(date, 1L))
                    .thenReturn(Optional.of(existing));
            when(resourceSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.updateResourceSnapshot(date, 1L);

            ArgumentCaptor<DailyResourceSnapshot> captor =
                    ArgumentCaptor.forClass(DailyResourceSnapshot.class);
            verify(resourceSnapshotRepo).save(captor.capture());
            assertThat(captor.getValue().getTotalDistributions()).isEqualTo(6); // 5 + 1
        }

        @Test
        @DisplayName("should create new snapshot with totalDistributions=1 when none exists")
        void noExistingSnapshot_shouldCreateWithCountOne() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            when(resourceSnapshotRepo.findBySnapshotDateAndWarehouseId(date, 2L))
                    .thenReturn(Optional.empty());
            when(resourceSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.updateResourceSnapshot(date, 2L);

            ArgumentCaptor<DailyResourceSnapshot> captor =
                    ArgumentCaptor.forClass(DailyResourceSnapshot.class);
            verify(resourceSnapshotRepo).save(captor.capture());

            DailyResourceSnapshot saved = captor.getValue();
            assertThat(saved.getSnapshotDate()).isEqualTo(date);
            assertThat(saved.getWarehouseId()).isEqualTo(2L);
            // @Builder.Default is 0, then service increments to 1
            assertThat(saved.getTotalDistributions()).isEqualTo(1);
        }

        @Test
        @DisplayName("should skip and not save when warehouseId is null")
        void nullWarehouseId_shouldSkipSilently() {
            LocalDate date = LocalDate.of(2025, 1, 15);

            reportService.updateResourceSnapshot(date, null);

            // Defensive guard in service — no DB interaction at all
            verify(resourceSnapshotRepo, never()).findBySnapshotDateAndWarehouseId(any(), any());
            verify(resourceSnapshotRepo, never()).save(any());
        }
    }

    // =====================================================================
    // updateTeamPerformance() — tested via updateRequestSnapshot()
    // since it is a private method called internally
    // =====================================================================

    @Nested
    @DisplayName("updateTeamPerformance() — via updateRequestSnapshot()")
    class UpdateTeamPerformance {

        @Test
        @DisplayName("should set avgDurationMin to first duration on first mission completion")
        void firstMission_shouldSetAvgToFirstDuration() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            // Request snapshot setup
            when(requestSnapshotRepo.findBySnapshotDate(date))
                    .thenReturn(Optional.of(buildRequestSnapshot(date, 0, null)));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Fresh team snapshot — missionsCompleted=0, avgDurationMin=null
            when(teamPerformanceRepo.findBySnapshotDateAndTeamId(date, 5L))
                    .thenReturn(Optional.empty());
            when(teamPerformanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT — first mission, 90 minutes
            reportService.updateRequestSnapshot(date, 5L, 90);

            ArgumentCaptor<TeamPerformanceSnapshot> captor =
                    ArgumentCaptor.forClass(TeamPerformanceSnapshot.class);
            verify(teamPerformanceRepo).save(captor.capture());

            TeamPerformanceSnapshot saved = captor.getValue();
            assertThat(saved.getMissionsCompleted()).isEqualTo(1);
            // First mission: avg = (0 * 0 + 90) / 1 = 90.00
            assertThat(saved.getAvgDurationMin()).isEqualByComparingTo(new BigDecimal("90.00"));
        }

        @Test
        @DisplayName("should recalculate team avgDurationMin correctly as running average")
        void subsequentMission_shouldRecalculateCorrectly() {
            // ARRANGE — team already has 4 missions with avg 50.00 min
            // Formula: (50.00 * 4 + 100) / 5 = 300 / 5 = 60.00
            LocalDate date = LocalDate.of(2025, 1, 15);
            when(requestSnapshotRepo.findBySnapshotDate(date))
                    .thenReturn(Optional.of(buildRequestSnapshot(date, 4, BigDecimal.valueOf(50.00))));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TeamPerformanceSnapshot existingTeamSnap =
                    buildTeamSnapshot(date, 5L, 4, BigDecimal.valueOf(50.00));
            when(teamPerformanceRepo.findBySnapshotDateAndTeamId(date, 5L))
                    .thenReturn(Optional.of(existingTeamSnap));
            when(teamPerformanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT — 5th mission, 100 minutes
            reportService.updateRequestSnapshot(date, 5L, 100);

            ArgumentCaptor<TeamPerformanceSnapshot> captor =
                    ArgumentCaptor.forClass(TeamPerformanceSnapshot.class);
            verify(teamPerformanceRepo).save(captor.capture());

            TeamPerformanceSnapshot saved = captor.getValue();
            assertThat(saved.getMissionsCompleted()).isEqualTo(5);
            assertThat(saved.getAvgDurationMin()).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("should increment missionsCompleted but keep avgDurationMin unchanged when duration is null")
        void nullDuration_shouldIncrementMissionsOnly() {
            LocalDate date = LocalDate.of(2025, 1, 15);
            when(requestSnapshotRepo.findBySnapshotDate(date))
                    .thenReturn(Optional.of(buildRequestSnapshot(date, 2, null)));
            when(requestSnapshotRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TeamPerformanceSnapshot existingSnap =
                    buildTeamSnapshot(date, 5L, 2, BigDecimal.valueOf(30.00));
            when(teamPerformanceRepo.findBySnapshotDateAndTeamId(date, 5L))
                    .thenReturn(Optional.of(existingSnap));
            when(teamPerformanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            reportService.updateRequestSnapshot(date, 5L, null);

            ArgumentCaptor<TeamPerformanceSnapshot> captor =
                    ArgumentCaptor.forClass(TeamPerformanceSnapshot.class);
            verify(teamPerformanceRepo).save(captor.capture());

            TeamPerformanceSnapshot saved = captor.getValue();
            assertThat(saved.getMissionsCompleted()).isEqualTo(3);
            // avgDurationMin must stay the same when duration is unknown
            assertThat(saved.getAvgDurationMin()).isEqualByComparingTo(new BigDecimal("30.00"));
        }
    }
}