package com.monew.monew_server.domain.interest;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.repository.InterestRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class InterestRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
        .withInitScript("init_fuzzystrmatch.sql");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private InterestRepository interestRepository;

    @Test
    @DisplayName("findSimilarInterests: 'Authentication' (길이 14) 기준으로 0.8 초과 항목만 필터링")
    void findSimilarInterests() {
        // given: DB에 다양한 유사도의 단어들을 저장
        interestRepository.save(Interest.builder().name("Authentication").build());
        interestRepository.save(Interest.builder().name("Authenticatian").build());
        interestRepository.save(Interest.builder().name("Authencicazion").build());
        interestRepository.save(Interest.builder().name("Authorization").build());
        interestRepository.save(Interest.builder().name("Microservice").build());
        interestRepository.flush();

        // when: "Authentication"으로 검색
        List<Interest> results = interestRepository.findSimilarInterests("Authentication");

        // then:
        // "Authentication" (Score 1.0),
        // "Authenticatian" (Score 0.928),
        // "Authencicazion" (Score 0.857) 3개가 반환되어야 한다.
        assertThat(results).hasSize(3);
        assertThat(results)
            .map(Interest::getName)
            .containsExactlyInAnyOrder("Authentication", "Authenticatian", "Authencicazion");
    }
}
