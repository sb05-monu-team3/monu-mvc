package com.monew.monew_server.domain.interest.dto;

import java.util.List;
import java.util.UUID;

public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    Integer subscriberCount,
    Boolean subscribedByMe
) {}
