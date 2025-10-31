package com.monew.monew_server.domain.interest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.monew.monew_server.domain.interest.enums.InterestSortField;
import java.time.Instant;

public record InterestQuery(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    Instant after,
    Integer limit,

    @JsonIgnore
    InterestSortField sortField,

    @JsonIgnore
    Boolean desc
) {
    public static final String DEFAULT_ORDER_BY = "name";
    public static final String DEFAULT_DIRECTION = "DESC";
    public static final int DEFAULT_LIMIT = 6;

    public InterestQuery {
        if (orderBy == null) {
            orderBy = DEFAULT_ORDER_BY;
        }
        if (direction == null) {
            direction = DEFAULT_DIRECTION;
        }
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        sortField = InterestSortField.from(orderBy);
        desc = "desc".equalsIgnoreCase(direction);
    }
}
