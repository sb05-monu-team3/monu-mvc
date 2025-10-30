package com.monew.monew_server.domain.interest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monew.monew_server.domain.interest.controller.InterestController;
import com.monew.monew_server.domain.interest.dto.InterestDto;
import com.monew.monew_server.domain.interest.dto.InterestRegisterRequest;
import com.monew.monew_server.domain.interest.service.InterestService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @Test
    @DisplayName("POST /api/interests - 관심사 생성 성공 (200 OK)")
    void create_Success() throws Exception {
        InterestRegisterRequest request = new InterestRegisterRequest(
            "Spring Boot",
            List.of("Java", "JPA")
        );

        InterestDto responseDto = new InterestDto(
            UUID.randomUUID(),
            "Spring Boot",
            List.of("Java", "JPA"),
            0,
            null
        );

        when(interestService.create(any(InterestRegisterRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(responseDto.id().toString()))
            .andExpect(jsonPath("$.name").value("Spring Boot"))
            .andExpect(jsonPath("$.keywords[0]").value("Java"));
    }

    @Test
    @DisplayName("POST /api/interests - 유효성 검사 실패 (이름 없음) (400 Bad Request)")
    void create_Fail_NullName() throws Exception {
        InterestRegisterRequest request = new InterestRegisterRequest(
            null,
            List.of("Java", "JPA")
        );

        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/interests - 유효성 검사 실패 (키워드 리스트가 null) (400 Bad Request)")
    void createInterest_ValidationFails_EmptyKeywords() throws Exception {
        InterestRegisterRequest request = new InterestRegisterRequest(
            "Spring Boot",
            null
        );

        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/interests - 유효성 검사 실패 (키워드 리스트가 비어있음) (400 Bad Request)")
    void create_Fail_EmptyKeywords() throws Exception {
        InterestRegisterRequest request = new InterestRegisterRequest(
            "Spring Boot",
            Collections.emptyList()
        );

        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
