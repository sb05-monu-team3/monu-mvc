package com.monew.monew_server.domain.interest.repository;

import com.monew.monew_server.domain.interest.dto.CursorPageResponseInterestDto;
import com.monew.monew_server.domain.interest.dto.InterestQuery;
import java.util.UUID;

public interface InterestQueryRepository {

    CursorPageResponseInterestDto findAll(InterestQuery query, UUID userId);
}
