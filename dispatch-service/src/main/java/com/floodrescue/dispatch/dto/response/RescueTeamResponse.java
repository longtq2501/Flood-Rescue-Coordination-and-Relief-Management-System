package com.floodrescue.dispatch.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.floodrescue.dispatch.domain.enums.TeamStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueTeamResponse {
    private Long id;
    private String name;
    private Long leaderId;
    private Integer capacity;
    private TeamStatus status;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private LocalDateTime createdAt;
    private List<TeamMemberResponse> members;
}
