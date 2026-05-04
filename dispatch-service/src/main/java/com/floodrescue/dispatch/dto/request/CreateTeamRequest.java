package com.floodrescue.dispatch.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeamRequest {
    @NotBlank(message = "Tên đội không được để trống")
    private String name;

    @NotNull(message = "ID đội trưởng không được để trống")
    private Long leaderId;

    @Min(value = 1, message = "Sức chứa tối thiểu là 1")
    private Integer capacity;
}
