package com.floodrescue.dispatch.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MapDataResponse {
    private List<TeamLocationDto> teams;

    @Data
    @Builder
    public static class TeamLocationDto {
        private Long teamId;
        private String teamName;
        private String status;
        private BigDecimal lat;
        private BigDecimal lng;
        private LocalDateTime lastUpdated;
    }
}
