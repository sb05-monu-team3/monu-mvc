package com.monew.monew_server.domain.interest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monew.monew_server.domain.interest.controller.InterestController;
import com.monew.monew_server.domain.interest.dto.CursorPageResponseInterestDto;
import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.dto.InterestQuery;
import com.monew.monew_server.domain.interest.dto.InterestRegisterRequest;
import com.monew.monew_server.domain.interest.service.InterestService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterestController.class)
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    private static final String HEADER_USER_ID = "Monew-Request-User-Id";
    private UUID userId;
    private InterestDto interestDto;
    private CursorPageResponseInterestDto cursorPageResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        interestDto = new InterestDto(
            UUID.randomUUID(),
            "Test Interest",
            List.of("keyword1", "keyword2"),
            10L,
            true
        );

        cursorPageResponseDto = new CursorPageResponseInterestDto(
            List.of(interestDto),
            "Test Interest",
            Instant.now(),
            1,
            1L,
            false
        );
    }

    @Test
    @DisplayName("GET /api/interests - 성공 (200 OK): 관심사 목록 조회")
    void findAll_success() throws Exception {
        // given
        when(interestService.findAll(any(InterestQuery.class), eq(userId)))
            .thenReturn(cursorPageResponseDto);
        ArgumentCaptor<InterestQuery> queryCaptor = ArgumentCaptor.forClass(InterestQuery.class);

        // when & then
        mockMvc.perform(get("/api/interests")
                .header(HEADER_USER_ID, userId.toString())
                .param("keyword", "Test")
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value(interestDto.name()))
            .andExpect(jsonPath("$.totalElements").value(1L));

        // verify: 서비스 호출 및 @ModelAttribute 바인딩 검증
        verify(interestService).findAll(queryCaptor.capture(), eq(userId));
        InterestQuery capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery.keyword()).isEqualTo("Test");
        assertThat(capturedQuery.limit()).isEqualTo(10);
    }

    @Test
    @DisplayName("GET /api/interests - 실패 (400 Bad Request): 필수 헤더 누락")
    void findAll_fail_missingHeader() throws Exception {
        // when & then
        mockMvc.perform(get("/api/interests")
                .param("keyword", "Test")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print());

        // verify: 헤더 누락 시 서비스가 호출되지 않는지 검증
        verify(interestService, never()).findAll(any(), any());
    }

    @Test
    @DisplayName("POST /api/interests - 성공 (201 Created): 관심사 생성")
    void create_success() throws Exception {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest("New Interest", List.of("k1"));
        InterestDto createdDto = new InterestDto(UUID.randomUUID(), "New Interest", List.of("k1"), 0L, true);

        when(interestService.create(any(InterestRegisterRequest.class)))
            .thenReturn(createdDto);

        ArgumentCaptor<InterestRegisterRequest> requestCaptor = ArgumentCaptor.forClass(InterestRegisterRequest.class);

        // when & then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Interest"));

        // verify: 서비스 호출 및 @RequestBody 바인딩 검증
        verify(interestService).create(requestCaptor.capture());
        assertThat(requestCaptor.getValue().name()).isEqualTo("New Interest");
    }

    @DisplayName("POST /api/interests - 실패 (400 Bad Request): 유효성 검사 실패")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidInterestRequests")
    void create_fail_validation(String testName, InterestRegisterRequest invalidRequest) throws Exception {
        // when & then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andDo(print());

        // verify: 유효성 검사 실패 시 서비스가 호출되지 않는지 검증
        verify(interestService, never()).create(any());
    }

    private static Stream<Arguments> invalidInterestRequests() {
        String nameTooLong = "a".repeat(51);
        List<String> keywordsTooMany = IntStream.range(0, 11)
            .mapToObj(i -> "k" + i)
            .toList();

        return Stream.of(
            Arguments.of("name: null", new InterestRegisterRequest(null, List.of("k1"))),
            Arguments.of("name: \"\" (min=1 위반)", new InterestRegisterRequest("", List.of("k1"))),
            Arguments.of("name: 51자 (max=50 위반)", new InterestRegisterRequest(nameTooLong, List.of("k1"))),
            Arguments.of("keywords: null", new InterestRegisterRequest("Valid Name", null)),
            Arguments.of("keywords: 0개 (min=1 위반)", new InterestRegisterRequest("Valid Name", Collections.emptyList())),
            Arguments.of("keywords: 11개 (max=10 위반)", new InterestRegisterRequest("Valid Name", keywordsTooMany))
        );
    }
}
