package com.floodrescue.dispatch.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberResponse {
    private Long userId;
    private LocalDateTime joinedAt;
}
