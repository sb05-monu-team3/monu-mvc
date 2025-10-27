package com.monew.monew_server.domain.comment.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;  // 추가
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private UUID id;
    private UUID articleId;
    private UUID userId;
    private String userNickname;
    private String content;
    private Long likeCount;
    private boolean likedByMe;
    private Instant createdAt;


}
