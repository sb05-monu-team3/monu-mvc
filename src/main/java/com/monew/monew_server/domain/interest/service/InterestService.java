package com.monew.monew_server.domain.interest.service;

import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.dto.InterestRegisterRequest;
import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.mapper.InterestMapper;
import com.monew.monew_server.domain.interest.repository.InterestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;

    private final InterestMapper interestMapper;

    public InterestDto create(InterestRegisterRequest request) {
        String name = request.name();
        List<String> keywords = request.keywords();

        Interest interest = interestRepository.save(
            Interest.builder()
                .name(name)
                .build()
        );
        return null;
    }
}
