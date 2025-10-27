package com.monew.monew_server.domain.comment.repository;

import com.monew.monew_server.domain.comment.dto.CommentDto;
import com.monew.monew_server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByArticle_Id(UUID articleId);
}
