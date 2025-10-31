package com.monew.monew_server.domain.interest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.dto.InterestRegisterRequest;
import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.entity.InterestKeyword;
import com.monew.monew_server.domain.interest.repository.InterestKeywordRepository;
import com.monew.monew_server.domain.interest.repository.InterestRepository;
import com.monew.monew_server.domain.interest.service.InterestService;
import com.monew.monew_server.exception.ErrorCode;
import com.monew.monew_server.exception.InterestException;
import java.util.List;
import java.util.UUID;
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
class InterestServiceTest {

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
    private InterestService interestService;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private InterestKeywordRepository interestKeywordRepository;

    @Test
    @DisplayName("create - 관심사 생성 성공 (유사한 이름 없음)")
    void create_Success_WhenNoSimilarNameExists() {
        // given
        // 'Python'이 'Java'와 유사하지 않음 (similarity < 0.8)
        interestRepository.save(Interest.builder().name("Python").build());

        InterestRegisterRequest request = new InterestRegisterRequest(
            "Java",
            List.of("OOP", "Spring")
        );

        // when
        InterestDto resultDto = interestService.create(request);

        // then
        // 1. DTO 검증
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.id()).isNotNull();
        assertThat(resultDto.name()).isEqualTo("Java");
        assertThat(resultDto.keywords()).containsExactlyInAnyOrder("OOP", "Spring");

        // 2. DB 검증 (Interest)
        List<Interest> interests = interestRepository.findAll();
        assertThat(interests).hasSize(2); // Python, Java
        Interest savedInterest = interests.stream()
            .filter(i -> i.getName().equals("Java"))
            .findFirst()
            .orElse(null);
        assertThat(savedInterest).isNotNull();

        // 3. DB 검증 (Keywords)
        assertThat(interestKeywordRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("create - 관심사 생성 실패 (유사한 이름 중복)")
    void create_Fail_WhenSimilarNameExists() {
        // given
        // 'Spring Boot'는 'SpringBoot'와 유사함 (similarity > 0.8)
        interestRepository.save(Interest.builder().name("Spring Boot").build());

        InterestRegisterRequest request = new InterestRegisterRequest(
            "SpringBoot", // 네이티브 쿼리가 'Spring Boot'와 유사하다고 판단해야 함
            List.of("Java", "Microservice")
        );

        // when & then
        // 1. 예외 발생 검증
        InterestException exception = assertThrows(InterestException.class, () -> interestService.create(request));

        // 2. 예외 타입 및 상세 정보 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTEREST_NAME_DUPLICATION);
        assertThat(exception.getDetails()).containsEntry("name", "SpringBoot");

        // 3. DB 검증 (새로운 데이터가 롤백/저장되지 않았는지 확인)
        assertThat(interestRepository.findAll()).hasSize(1); // 기존 'Spring Boot'만 존재
        assertThat(interestKeywordRepository.findAll()).isEmpty(); // 키워드가 저장되지 않음
    }

    @Test
    @DisplayName("Lombok 및 Record 커버리지를 위한 코드 실행")
    void testRecordAndEntityCoverage() {

        InterestDto dto1 = new InterestDto(UUID.randomUUID(), "Name", List.of("k1"), 1L, true);
        InterestDto dto2 = new InterestDto(dto1.id(), "Name", List.of("k1"), 1L, true);

        assertThat(dto1.id()).isNotNull();
        assertThat(dto1.name()).isEqualTo("Name");
        assertThat(dto1.keywords()).contains("k1");
        assertThat(dto1.subscriberCount()).isEqualTo(1);
        assertThat(dto1.subscribedByMe()).isTrue();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).isNotNull();

        Interest interest = Interest.builder().name("Test").build();
        interest.setName("New Name"); // setter
        assertThat(interest.getName()).isEqualTo("New Name"); // getter

        InterestKeyword keyword = InterestKeyword.builder()
            .id(UUID.randomUUID())
            .name("kw")
            .interest(interest)
            .build();

        assertThat(interest.toString()).isNotNull();
        assertThat(keyword.toString()).isNotNull();
    }
}
