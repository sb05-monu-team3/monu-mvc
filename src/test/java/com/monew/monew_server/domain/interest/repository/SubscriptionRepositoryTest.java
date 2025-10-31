package com.monew.monew_server.domain.interest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.entity.Subscription;
import com.monew.monew_server.domain.user.entity.User;
import com.monew.monew_server.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
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
class SubscriptionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InterestRepository interestRepository;
    @Autowired private EntityManager entityManager;

    private User user1, user2;
    private Interest interest1, interest2;
    private Subscription sub1;

    @BeforeEach
    void setUp() {
        subscriptionRepository.deleteAll();
        interestRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(User.builder()
            .email("test@test.com")
            .nickname("test")
            .password("test1234!")
            .build()
        );
        user2 = userRepository.save(User.builder()
            .email("test2@test.com")
            .nickname("test2")
            .password("test1234!")
            .build()
        );
        interest1 = interestRepository.save(Interest.builder().name("Interest 1").build());
        interest2 = interestRepository.save(Interest.builder().name("Interest 2").build());

        sub1 = subscriptionRepository.save(Subscription.builder().user(user1).interest(interest1).build());
        subscriptionRepository.save(Subscription.builder().user(user2).interest(interest1).build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByUserIdAndInterestId: 구독 정보가 존재할 때 Optional<Subscription> 반환")
    void findByUserIdAndInterestId_shouldReturnSubscription_whenExists() {
        Optional<Subscription> found = subscriptionRepository
            .findByUserIdAndInterestId(user1.getId(), interest1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(sub1.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
        assertThat(found.get().getInterest().getId()).isEqualTo(interest1.getId());
    }

    @Test
    @DisplayName("findByUserIdAndInterestId: 구독 정보가 존재하지 않을 때 (잘못된 유저) Optional.empty 반환")
    void findByUserIdAndInterestId_shouldReturnEmpty_whenUserNotSubscribed() {
        Optional<Subscription> found = subscriptionRepository
            .findByUserIdAndInterestId(user1.getId(), interest2.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndInterestId: 구독 정보가 존재하지 않을 때 (잘못된 관심사) Optional.empty 반환")
    void findByUserIdAndInterestId_shouldReturnEmpty_whenInterestNotSubscribed() {
        Optional<Subscription> found = subscriptionRepository
            .findByUserIdAndInterestId(user2.getId(), interest2.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("countByInterestId: 구독자가 2명일 때 2L 반환")
    void countByInterestId_shouldReturnCorrectCount_whenMultipleSubscriptions() {
        long count = subscriptionRepository.countByInterestId(interest1.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("countByInterestId: 구독자가 0명일 때 0L 반환")
    void countByInterestId_shouldReturnZero_whenNoSubscriptions() {
        long count = subscriptionRepository.countByInterestId(interest2.getId());

        assertThat(count).isZero();
    }
}