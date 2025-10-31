package com.monew.monew_server.domain.interest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.entity.InterestKeyword;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
class InterestKeywordRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired private InterestKeywordRepository interestKeywordRepository;
    @Autowired private InterestRepository interestRepository;
    @Autowired private EntityManager entityManager;

    private Interest interest1, interest2, interest3;

    @BeforeEach
    void setUp() {
        interestKeywordRepository.deleteAll();
        interestRepository.deleteAll();

        interest1 = interestRepository.save(Interest.builder().name("Java").build());
        interest2 = interestRepository.save(Interest.builder().name("Spring").build());
        interest3 = interestRepository.save(Interest.builder().name("Python").build());

        interestKeywordRepository.save(InterestKeyword.builder().name("JVM").interest(interest1).build());
        interestKeywordRepository.save(InterestKeyword.builder().name("OOP").interest(interest1).build());

        interestKeywordRepository.save(InterestKeyword.builder().name("Boot").interest(interest2).build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findKeywordsByInterestId: 여러 개의 키워드가 존재할 때 List<String> 반환")
    void findKeywordsByInterestId_shouldReturnKeywordNames_whenMultipleKeywordsExist() {
        List<String> keywords = interestKeywordRepository.findKeywordsByInterestId(interest1.getId());

        assertThat(keywords)
            .isNotNull()
            .hasSize(2)
            .containsExactlyInAnyOrder("JVM", "OOP");
    }

    @Test
    @DisplayName("findKeywordsByInterestId: 한 개의 키워드가 존재할 때 List<String> 반환")
    void findKeywordsByInterestId_shouldReturnKeywordName_whenOneKeywordExists() {
        List<String> keywords = interestKeywordRepository.findKeywordsByInterestId(interest2.getId());

        assertThat(keywords)
            .isNotNull()
            .hasSize(1)
            .containsExactly("Boot");
    }

    @Test
    @DisplayName("findKeywordsByInterestId: 키워드가 존재하지 않을 때 빈 리스트(empty list) 반환")
    void findKeywordsByInterestId_shouldReturnEmptyList_whenNoKeywordsExist() {
        List<String> keywords = interestKeywordRepository.findKeywordsByInterestId(interest3.getId());

        assertThat(keywords)
            .isNotNull()
            .isEmpty();
    }
}