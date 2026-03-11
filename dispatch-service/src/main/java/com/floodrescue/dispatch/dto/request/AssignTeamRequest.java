package com.floodrescue.dispatch.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTeamRequest {

    @NotNull(message = "requestId không được để trống")
    private Long requestId;

    @NotNull(message = "teamId không được để trống")
    private Long teamId;

    @NotNull(message = "vehicleId không được để trống")
    private Long vehicleId;

    @NotNull(message = "citizenId không được để trống")
    private Long citizenId;
}
