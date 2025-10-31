package com.monew.monew_server.domain.comment.controller;
import com.monew.monew_server.domain.comment.dto.CommentDto;
import com.monew.monew_server.domain.comment.dto.CommentRegisterRequest;
import com.monew.monew_server.domain.comment.dto.CommentUpdateRequest;
import com.monew.monew_server.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;



@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * POST /api/comments
     *
     * @param request 댓글 등록 요청 정보
     * @return 생성된 댓글 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CommentRegisterRequest request
    ) {
        log.info("POST /api/comments - 댓글 생성 요청");

        CommentDto createdComment =
                commentService.createComment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdComment);
    }


    // 기본 댓글 조회 (단순 버전 - 현재 사용 중)
    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(
            @RequestParam UUID articleId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        log.info("GET /api/comments - articleId={}, orderBy={}, direction={}, cursor={}, limit={}",
                articleId, orderBy, direction, cursor, limit);

        List<CommentDto> comments = commentService.getComments(
                articleId, orderBy, direction, cursor, after, limit
        );

        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID commentId,
            @RequestHeader("Monew-Request-User-ID") UUID userId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        log.info("PATCH /api/comments/{} - userId={}", commentId, userId);

        CommentDto updatedComment = commentService.updateComment(commentId, userId, request);

        return ResponseEntity.ok(updatedComment);
    }



    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        log.info("DELETE /api/comments/{} - 논리 삭제 요청, userId={}", commentId, userId);

        commentService.deleteComment(commentId, userId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> deleteHardComment(
            @PathVariable UUID commentId,
            @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        log.info("DELETE /api/comments/{}/hard - 물리 삭제 요청, userId={}", commentId, userId);

        commentService.hardDeleteComment(commentId, userId);

        return ResponseEntity.noContent().build();
    }


}
