package com.monew.monew_server.domain.common.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {

    //실제 데이터 목록
    private List<T> content;

    private String nextCursor;

    private Instant nextAfter;

    private int size;

    private long totalElements;

    private boolean hasNext;
}
