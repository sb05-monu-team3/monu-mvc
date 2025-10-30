package com.monew.monew_server.domain.interest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InterestRegisterRequest(
    @NotNull
    @Size(min = 1, max = 50)
    String name,

    @NotNull
    @Size(min = 1, max = 10)
    List<String> keywords
) {}
