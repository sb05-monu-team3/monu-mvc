package com.monew.monew_server.domain.interest;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.entity.Interest;
import com.monew.monew_server.domain.interest.mapper.InterestMapper;
import com.monew.monew_server.domain.interest.mapper.InterestMapperImpl;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InterestMapperImpl.class})
class InterestMapperTest {

    @Autowired
    private InterestMapper interestMapper;

    @Test
    @DisplayName("toDto: 모든 필드가 정상적으로 DTO에 매핑되어야 한다 (Happy Path)")
    void toDto_shouldMapAllFieldsCorrectly() {
        // given
        Interest interest = Interest.builder().name("Spring").build();
        List<String> keywords = List.of("k1", "k2");
        Integer subscriberCount = 10;
        Boolean subscribedByMe = true;

        // when
        InterestDto dto = interestMapper.toDto(interest, keywords, subscriberCount, subscribedByMe);

        // then
        assertThat(dto).isNotNull();
        // 1. Interest 객체 매핑 검증
        assertThat(dto.id()).isEqualTo(interest.getId()); // BaseEntity의 ID (null일 수 있음)
        assertThat(dto.name()).isEqualTo("Spring");
        // 2. keywords 매핑 검증 (MapStruct는 새 리스트를 생성)
        assertThat(dto.keywords()).isEqualTo(keywords);
        assertThat(dto.keywords()).isNotSameAs(keywords); // 원본 리스트와 다른 인스턴스인지 확인
        // 3. subscriberCount 매핑 검증 (Integer -> Long 변환 검증)
        assertThat(dto.subscriberCount()).isEqualTo(10L);
        // 4. subscribedByMe 매핑 검증
        assertThat(dto.subscribedByMe()).isTrue();
    }

    @Test
    @DisplayName("toDto: 모든 입력 파라미터가 null일 때 null을 반환해야 한다")
    void toDto_shouldReturnNull_whenAllInputsAreNull() {
        // given: 모든 파라미터가 null
        // when
        InterestDto dto = interestMapper.toDto(null, null, null, null);

        // then
        assertThat(dto).isNull(); // "if ( interest == null && keywords == null ... )" 분기 커버
    }

    @Test
    @DisplayName("toDto: Interest 객체만 null일 때, DTO의 id와 name은 null이어야 한다")
    void toDto_shouldHandleNullInterest() {
        // given
        List<String> keywords = List.of("k1");
        Integer subscriberCount = 5;
        Boolean subscribedByMe = false;

        // when
        InterestDto dto = interestMapper.toDto(null, keywords, subscriberCount, subscribedByMe);

        // then
        assertThat(dto).isNotNull();
        // "if ( interest != null )" 분기(false) 커버
        assertThat(dto.id()).isNull();
        assertThat(dto.name()).isNull();
        // 나머지 필드는 정상 매핑
        assertThat(dto.keywords()).isEqualTo(keywords);
        assertThat(dto.subscriberCount()).isEqualTo(5L);
        assertThat(dto.subscribedByMe()).isFalse();
    }

    @Test
    @DisplayName("toDto: keywords 리스트가 null일 때, DTO의 keywords는 null이어야 한다")
    void toDto_shouldHandleNullKeywords() {
        // given
        Interest interest = Interest.builder().name("Java").build();

        // when
        InterestDto dto = interestMapper.toDto(interest, null, 5, true);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.name()).isEqualTo("Java");
        // "if ( list != null )" 분기(false) 커버
        assertThat(dto.keywords()).isNull();
        assertThat(dto.subscriberCount()).isEqualTo(5L);
        assertThat(dto.subscribedByMe()).isTrue();
    }

    @Test
    @DisplayName("toDto: subscriberCount가 null일 때, DTO의 subscriberCount는 null이어야 한다")
    void toDto_shouldHandleNullSubscriberCount() {
        // given
        Interest interest = Interest.builder().name("Java").build();

        // when
        InterestDto dto = interestMapper.toDto(interest, List.of("k1"), null, true);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.name()).isEqualTo("Java");
        assertThat(dto.keywords()).containsExactly("k1");
        // "if ( subscriberCount != null )" 분기(false) 커버
        assertThat(dto.subscriberCount()).isNull();
        assertThat(dto.subscribedByMe()).isTrue();
    }
}