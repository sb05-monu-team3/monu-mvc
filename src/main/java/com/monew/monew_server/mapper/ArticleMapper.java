package com.monew.monew_server.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.dto.ArticleResponse;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

	ArticleMapper INSTANCE = Mappers.getMapper(ArticleMapper.class);

	@Mapping(target = "commentCount", ignore = true)
	@Mapping(target = "viewCount", ignore = true)
	@Mapping(target = "viewedByMe", ignore = true)
	ArticleResponse toResponse(Article article);

	List<ArticleResponse> toResponseList(List<Article> articles);
}
