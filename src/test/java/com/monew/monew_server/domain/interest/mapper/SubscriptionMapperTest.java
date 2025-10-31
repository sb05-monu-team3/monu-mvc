package com.monew.monew_server.domain.interest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.monew_server.domain.interest.dto.SubscriptionDto;
import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.entity.Subscription;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SubscriptionMapperImpl.class})
public class SubscriptionMapperTest {

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Test
    @DisplayName("toDto: 모든 필드가 DTO에 정상적으로 매핑되어야 한다 (Happy Path)")
    void toDto_shouldMapAllFieldsCorrectly() {
        Instant now = Instant.now();
        UUID interestId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        Interest interest = Interest.builder().id(interestId).name("JPA").build();
        Subscription subscription = Subscription.builder()
            .id(subscriptionId)
            .interest(interest)
            .createdAt(now)
            .build();

        List<String> keywords = List.of("ORM", "QueryDSL");
        Long subscriberCount = 120L;

        SubscriptionDto dto = subscriptionMapper.toDto(subscription, keywords, subscriberCount);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(subscriptionId);
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.interestId()).isEqualTo(interestId);
        assertThat(dto.interestName()).isEqualTo("JPA");
        assertThat(dto.interestKeywords()).isEqualTo(keywords);
        assertThat(dto.interestSubscriberCount()).isEqualTo(120L);
    }

    @Test
    @DisplayName("toDto: 모든 입력 파라미터가 null일 때 null을 반환해야 한다")
    void toDto_shouldReturnNull_whenAllInputsAreNull() {
        SubscriptionDto dto = subscriptionMapper.toDto(null, null, null);
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toDto: Subscription 객체가 null일 때, 관련 필드는 null이어야 한다")
    void toDto_shouldHandleNullSubscription() {
        List<String> keywords = List.of("k1");
        Long subscriberCount = 10L;

        SubscriptionDto dto = subscriptionMapper.toDto(null, keywords, subscriberCount);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNull();
        assertThat(dto.createdAt()).isNull();
        assertThat(dto.interestId()).isNull();
        assertThat(dto.interestName()).isNull();
        assertThat(dto.interestKeywords()).isEqualTo(keywords);
        assertThat(dto.interestSubscriberCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("toDto: Subscription.Interest가 null일 때, 관련 필드는 null이어야 한다")
    void toDto_shouldHandleNullInterestInSubscription() {
        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .interest(null)
            .build();

        SubscriptionDto dto = subscriptionMapper.toDto(subscription, List.of("k1"), 10L);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(subscription.getId());
        assertThat(dto.interestId()).isNull();
        assertThat(dto.interestName()).isNull();
        assertThat(dto.interestKeywords()).containsExactly("k1");
        assertThat(dto.interestSubscriberCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("toDto: keywords가 null일 때, interestKeywords는 null이어야 한다")
    void toDto_shouldHandleNullKeywords() {
        Interest interest = Interest.builder().name("JPA").build();
        Subscription subscription = Subscription.builder().interest(interest).build();

        SubscriptionDto dto = subscriptionMapper.toDto(subscription, null, 10L);

        assertThat(dto).isNotNull();
        assertThat(dto.interestName()).isEqualTo("JPA");
        assertThat(dto.interestKeywords()).isNull();
        assertThat(dto.interestSubscriberCount()).isEqualTo(10L);
    }
}
