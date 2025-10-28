package com.monew.monew_server.domain.comment.controller;

import com.monew.monew_server.domain.comment.dto.CommentDto;
import
        com.monew.monew_server.domain.comment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;


    // 댓글 생성 테스트 (댓글 생성 성공)
    @Test // Junit 에게 이건 테스트라고 알려주는것
    @DisplayName("POST /api/comments - 댓글 생성 성공") // 테스트 결과를 한글로 표시
    void createComment_Success() throws Exception { // MockMvc가 던질수 있는 예외 처리
        // Given 테스트 데이터 준비
        UUID articleId = UUID.randomUUID(); // 테스트용 랜덤뉴스기사생성
        UUID userId = UUID.randomUUID(); // 테스트용 랜덤유저생성
        String content = "댓글 테스트 123";

        // Mock 응답 데이터 (Service가 반환할 값)
        CommentDto mockResponse = CommentDto.builder()
                .id(UUID.randomUUID())
                .articleId(articleId)
                .userId(userId)
                .userNickname("테스터")
                .content(content)
                .likeCount(0L)  // ← 수정됨! 0L 추가
                .likedByMe(false)
                .createdAt(Instant.now())
                .build();

        // Service Mock 설정: "createComment가 호출되면 mockResponse를 반환해"
        given(commentService.createComment(any()))
                .willReturn(mockResponse);

        // When & Then: HTTP POST 요청 + 검증
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "articleId": "%s",
                                    "userId": "%s",
                                    "content": "%s"
                                }
                                """.formatted(articleId, userId,
                                content)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value(content))

                .andExpect(jsonPath("$.userNickname").value("테스터"));
    }

    @Test
    @DisplayName("PATCH /api/comments/{commentId} - 댓글 수정 성공")
    void updateComment_Success() throws Exception {
        // Given : 테스트 데이터 준비
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String updatedContent = "수정된 댓글 내용입니다.";

        // Mock 응답 데이터 (수정 후 반환될 댓글 응답)
        CommentDto mockResponce = CommentDto.builder()
                .id(commentId)
                .articleId(articleId)
                .userId(userId)
                .userNickname("테스터")
                .createdAt(Instant.now())
                .likeCount(0L)
                .likedByMe(false)
                .content(updatedContent) // 수정할 메세지는 정해졌으니 할당됨
                .build();

        // Service Mock 설정
        given(commentService.updateComment(eq(commentId), eq(userId),
                any())).willReturn(mockResponce);

        // When Then : HTTP PATCH 요청 + 검증 과정
// When & Then: HTTP PATCH 요청 + 검증
        mockMvc.perform(patch("/api/comments/{commentId}",
                        commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID",
                                userId.toString())
                        .content("""
                                {
                                    "content": "%s"
                                }
                                """.formatted(updatedContent)))
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(commentId.toString()))

                .andExpect(jsonPath("$.content").value(updatedContent));
    }


    @Test
    @DisplayName("GET /api/comments - 댓글 목록 조회 성공")
    void getComments_Success() throws Exception {
        // Given: 테스트 데이터 준비
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentDto comment1 = CommentDto.builder()
                .id(UUID.randomUUID())
                .articleId(articleId)
                .userId(userId)
                .userNickname("테스터1")
                .content("첫번째 댓글입니다")
                .likeCount(5L)
                .likedByMe(false)
                .createdAt(Instant.now())
                .build();

        List<CommentDto> mockComments = List.of(comment1);

        // Service Mock 설정: "이 메서드가 호출되면 이 값을 반환하라"
        given(commentService.getComments(any(), any(), any(),
                any(), any(),
                anyInt()))
                .willReturn(mockComments);

        // When & Then: HTTP 요청 후 응답 검증
        mockMvc.perform(get("/api/comments")
                        .param("articleId",
                                articleId.toString())
                        .param("orderBy", "createdAt")
                        .param("direction", "ASC")
                        .param("limit", "50")
                        .header("Monew-Request-User-ID",
                                userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("첫번째 댓글입니다"))
                                .andExpect(jsonPath("$[0].likeCount").value(5));
    }

}
