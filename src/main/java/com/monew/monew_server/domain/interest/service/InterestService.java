package com.monew.monew_server.domain.interest.service;

import com.monew.monew_server.domain.interest.dto.CursorPageResponseInterestDto;
import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.dto.InterestQuery;
import com.monew.monew_server.domain.interest.dto.InterestRegisterRequest;
import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.entity.InterestKeyword;
import com.monew.monew_server.domain.interest.mapper.InterestMapper;
import com.monew.monew_server.domain.interest.repository.InterestKeywordRepository;
import com.monew.monew_server.domain.interest.repository.InterestRepository;
import com.monew.monew_server.exception.ErrorCode;
import com.monew.monew_server.exception.InterestException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
    private final InterestKeywordRepository interestKeywordRepository;

    private final InterestMapper interestMapper;

    public CursorPageResponseInterestDto findAll(InterestQuery query, UUID userId) {
        return interestRepository.findAll(query, userId);
    }

    @Transactional
    public InterestDto create(InterestRegisterRequest request) {
        String name = request.name();
        List<String> keywords = request.keywords();

        List<Interest> similarInterests = interestRepository.findSimilarInterests(name);
        if (!similarInterests.isEmpty()) {
            throw new InterestException(
                ErrorCode.INTEREST_NAME_DUPLICATION,
                Map.of("name", name)
            );
        }

        Interest interest = interestRepository.save(
            Interest.builder()
                .name(name)
                .build()
        );

        List<InterestKeyword> interestKeywords = keywords.stream()
            .map(keywordName -> InterestKeyword.builder()
                .name(keywordName)
                .interest(interest)
                .build())
            .collect(Collectors.toList());

        List<InterestKeyword> savedKeywords = interestKeywordRepository.saveAll(interestKeywords);

        List<String> savedKeywordNames = savedKeywords.stream()
            .map(InterestKeyword::getName)
            .toList();

        return interestMapper.toDto(
            interest,
            savedKeywordNames,
            0,
            null
        );
    }
}
