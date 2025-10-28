package com.monew.monew_server.domain.comment.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRegisterRequest {

    // 댓글 작성시 기사 ID
    @NotNull(message = "기사 ID는 필수입니다.")
    private UUID articleId;

    // 댓글 작성자 ID
    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    // 댓글
    @NotNull(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하여야 합니다")
    private String content;
}
