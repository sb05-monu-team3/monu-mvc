package com.monew.monew_server.domain.comment.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter // Lombok이 자동으로 getContent() 메서드 생성을 위함
@NoArgsConstructor // 파라메터 없는 기본 생성자 생성 (Jackson Json 역직렬화에 필요하다)
@AllArgsConstructor // 모든 필드를 파라메터로 받는 생성자 생성 (테스트 작성에도 좋다고함)
public class CommentUpdateRequest {

    @NotNull(message = "댓글 내용은 필수입니다.") // 메시지는 Null이면 안됨.
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하여야 합니다.") // 댓글 조건에 포함된것
    private String content;

}
