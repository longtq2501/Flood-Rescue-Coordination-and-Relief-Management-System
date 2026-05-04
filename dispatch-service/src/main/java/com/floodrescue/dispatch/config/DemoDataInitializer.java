package com.floodrescue.dispatch.config;

import com.floodrescue.dispatch.domain.entity.RescueTeam;
import com.floodrescue.dispatch.domain.entity.TeamMember;
import com.floodrescue.dispatch.domain.enums.TeamStatus;
import com.floodrescue.dispatch.repository.RescueTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final RescueTeamRepository rescueTeamRepository;

    private static final List<DemoTeam> DEMO_TEAMS = List.of(
            new DemoTeam(
                    "Team Alpha",
                    2L,
                    TeamStatus.AVAILABLE,
                    "10.77245000",
                    "106.68212000",
                    2L
            ),
            new DemoTeam(
                    "Team Bravo",
                    3L,
                    TeamStatus.BUSY,
                    "10.75930000",
                    "106.69545000",
                    3L
            ),
            new DemoTeam(
                    "Team Charlie",
                    4L,
                    TeamStatus.RETURNING,
                    "10.74892000",
                    "106.70388000",
                    4L
            )
    );

    @Override
    public void run(String... args) {
                Set<String> existingTeamNames = rescueTeamRepository.findAll().stream()
                                .map(RescueTeam::getName)
                                .collect(Collectors.toSet());

                DEMO_TEAMS.stream()
                                .filter(team -> !existingTeamNames.contains(team.name()))
                                .forEach(this::seedTeamIfMissing);
    }

    private void seedTeamIfMissing(DemoTeam demoTeam) {
        RescueTeam team = RescueTeam.builder()
                .name(demoTeam.name())
                .leaderId(demoTeam.leaderId())
                .status(demoTeam.status())
                .capacity(4)
                .currentLat(new BigDecimal(demoTeam.lat()))
                .currentLng(new BigDecimal(demoTeam.lng()))
                .members(new ArrayList<>())
                .build();

        TeamMember leaderMember = TeamMember.builder()
                .team(team)
                .userId(demoTeam.memberUserId())
                .build();
        team.getMembers().add(leaderMember);

        rescueTeamRepository.save(team);

        log.info("Seeded rescue team demo: name={}, leaderId={}, status={}", demoTeam.name(), demoTeam.leaderId(), demoTeam.status());
    }

    private record DemoTeam(
            String name,
            Long leaderId,
            TeamStatus status,
            String lat,
            String lng,
            Long memberUserId
    ) {
    }
}