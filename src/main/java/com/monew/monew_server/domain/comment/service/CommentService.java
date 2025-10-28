package com.monew.monew_server.domain.comment.service;
import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.comment.dto.CommentDto;
import com.monew.monew_server.domain.comment.dto.CommentRegisterRequest;
import com.monew.monew_server.domain.comment.dto.CommentUpdateRequest;
import com.monew.monew_server.domain.comment.entity.Comment;
import com.monew.monew_server.domain.comment.repository.CommentRepository;
import com.monew.monew_server.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final EntityManager entityManager;

    @Transactional
    public CommentDto createComment(CommentRegisterRequest request) {
        log.info("댓글 생성 요청: articleId={}, userId={}",
                request.getArticleId(), request.getUserId());

        // 1. Article과 User 참조 가져오기 (프록시 객체)
        Article article = entityManager.getReference(Article.class, request.getArticleId());
        User user = entityManager.getReference(User.class, request.getUserId());

        // 2. Comment Entity 생성
        Comment comment = Comment.builder()
                .article(article)
                .user(user)
                .content(request.getContent())
                .build();

        // 3. DB 저장
        Comment savedComment = commentRepository.save(comment);
        log.info("댓글 생성 완료: commentId={}", savedComment.getId());

        // 4. Entity → DTO 변환
        return convertToDto(savedComment);
    }

    private CommentDto convertToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .articleId(comment.getArticle() != null ? comment.getArticle().getId() : null)
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userNickname(comment.getUser() != null ? comment.getUser().getNickname() : "임시닉네임")
                .content(comment.getContent())
                .likeCount(0L)
                .likedByMe(false)
                .createdAt(comment.getCreatedAt())
                .build();
    }
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(UUID articleId,String orderBy,String direction,
            String cursor, Instant after, int limit) {

        log.info("댓글 조회 요청: articleId={}", articleId);

        // 1단계: 일단 전체 조회 (나중에 정렬/페이징 추가 예정)
        List<Comment> comments = commentRepository.findByArticle_Id(articleId);

        log.info("조회된 댓글 수: {}", comments.size());

        // 2단계: Entity → DTO 변환
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public CommentDto updateComment(UUID commentId, UUID userId, CommentUpdateRequest request) {
        log.info("댓글 수정 요청: commentId={}, userId={}", commentId, userId);

        // 1. 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + commentId));

        // 2. 삭제된 댓글 체크
        if (comment.isDeleted()) {
            log.warn("삭제된 댓글 수정 시도: commentId={}", commentId);
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }

        // 3. 본인 확인 (작성자만 수정 가능)
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("권한 없음: 댓글 작성자({})와 요청자({})가 다름", comment.getUser().getId(), userId);
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 4. 내용 수정
        comment.setContent(request.getContent());
        // JPA의 더티 체킹: @Transactional 내에서 엔티티 변경 시 자동으로 UPDATE 쿼리 실행
        // save() 호출 불필요! (하지만 명시적으로 save()를 호출해도 문제 없다고함)

        log.info("댓글 수정 완료: commentId={}", commentId);

        // 5. DTO 변환 후 반환
        return convertToDto(comment);
    }

}
