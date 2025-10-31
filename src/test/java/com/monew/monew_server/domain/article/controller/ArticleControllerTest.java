package com.monew.monew_server.domain.article.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.dto.ArticleResponse;
import com.monew.monew_server.domain.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.domain.article.service.ArticleService;

@WebMvcTest(
	controllers = ArticleController.class,
	excludeAutoConfiguration = {
		JpaRepositoriesAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		SecurityAutoConfiguration.class
	}
)
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ArticleService articleService;

	private final UUID ARTICLE_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private final UUID ARTICLE_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

	@Test
	@DisplayName("전체 뉴스 목록 조회 성공")
	void shouldReturnArticles_whenGetArticles() throws Exception {

		ArticleResponse article1 = new ArticleResponse(
			ARTICLE_ID_1, "기사 제목 1", "기사 요약 1", "NAVER", Instant.now(), 10L, 2L, true
		);
		ArticleResponse article2 = new ArticleResponse(
			ARTICLE_ID_2, "기사 제목 2", "기사 요약 2", "CHOSUN", Instant.now(), 5L, 0L, false
		);

		CursorPageResponseArticleDto mockResponse = CursorPageResponseArticleDto.builder()
			.content(List.of(article1, article2))
			.hasNext(false)
			.size(10)
			.totalElements(2)
			.build();

		when(articleService.fetchArticles(any(ArticleRequest.class), any(UUID.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/articles")
				.param("keyword", "")
				.param("size", "10")
				.param("sortBy", "DATE")
				.param("cursor", "")
				.param("nextAfter", "")
				.header("Monew-Request-User-ID", "00000000-0000-0000-0000-000000000001")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].title").value("기사 제목 1"))
			.andExpect(jsonPath("$.content[1].sourceUrl").value("CHOSUN"));
	}

	@Test
	@DisplayName("검색어로 기사 제목/요약 필터링 테스트")
	void shouldFilterArticlesByKeyword() throws Exception {
		UUID articleId = UUID.fromString("00000000-0000-0000-0000-000000000004");
		ArticleResponse filteredArticle = new ArticleResponse(
			articleId, "삼성전자 실적 상승", "경제 뉴스", "HANKYUNG", Instant.now(), 3L, 1L, false
		);

		CursorPageResponseArticleDto mockResponse = CursorPageResponseArticleDto.builder()
			.content(List.of(filteredArticle))
			.hasNext(false)
			.size(10)
			.totalElements(1)
			.build();

		when(articleService.fetchArticles(any(ArticleRequest.class), any(UUID.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/articles")
				.param("keyword", "삼성전자")
				.param("size", "10")
				.param("sortBy", "DATE")
				.param("cursor", "")        // 빈 문자열 대신 null로 보내도 됨
				.param("nextAfter", "")     // 빈 문자열 대신 null로 보내도 됨
				.header("Monew-Request-User-ID", "00000000-0000-0000-0000-000000000001")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].title").value("삼성전자 실적 상승"));
	}

	@Test
	@DisplayName("정렬 조건(date) 테스트")
	void shouldSortArticlesByDate() throws Exception {
		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000011");

		ArticleResponse latestArticle = new ArticleResponse(
			id1, "최신 기사", "내용", "NAVER", Instant.now(), 1L, 0L, false
		);
		ArticleResponse olderArticle = new ArticleResponse(
			id2, "오래된 기사", "내용", "NAVER", Instant.now().minusSeconds(86400), 1L, 0L, false
		);

		List<ArticleResponse> sortedArticles = List.of(latestArticle, olderArticle);

		CursorPageResponseArticleDto mockResponse = CursorPageResponseArticleDto.builder()
			.content(sortedArticles)
			.hasNext(false)
			.size(10)
			.totalElements(2)
			.build();

		when(articleService.fetchArticles(any(ArticleRequest.class), any(UUID.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/articles")
				.param("sortBy", "DATE")
				.param("size", "10")
				.param("cursor", "")
				.param("nextAfter", "")
				.header("Monew-Request-User-ID", "00000000-0000-0000-0000-000000000001")
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].title").value("최신 기사"));
	}

	@Test
	@DisplayName("정렬 조건(viewcount) 테스트")
	void shouldSortArticlesByViewCount() throws Exception {
		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000020");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000021");

		ArticleResponse articleWithHighViews = new ArticleResponse(
			id1, "조회수 1위 기사", "내용", "NAVER", Instant.now(), 5L, 100L, false
		);
		ArticleResponse articleWithLowViews = new ArticleResponse(
			id2, "조회수 낮은 기사", "내용", "CHOSUN", Instant.now(), 1L, 10L, false
		);

		List<ArticleResponse> sortedArticles = List.of(articleWithHighViews, articleWithLowViews);

		CursorPageResponseArticleDto mockResponse = CursorPageResponseArticleDto.builder()
			.content(sortedArticles)
			.hasNext(false)
			.size(10)
			.totalElements(2)
			.build();

		when(articleService.fetchArticles(any(ArticleRequest.class), any(UUID.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/articles?sortBy=VIEW_COUNT")
				.header("Monew-Request-User-ID", "00000000-0000-0000-0000-000000000001"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].title").value("조회수 1위 기사"))
			.andExpect(jsonPath("$.content[0].viewCount").value(100));
	}

	@Test
	@DisplayName("정렬 조건(commentcount) 테스트")
	void shouldSortArticlesByCommentCount() throws Exception {
		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000030");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000031");

		ArticleResponse articleWithManyComments = new ArticleResponse(
			id1, "댓글 많은 기사", "내용", "NAVER", Instant.now(), 50L, 50L, false
		);
		ArticleResponse articleWithFewComments = new ArticleResponse(
			id2, "댓글 적은 기사", "내용", "CHOSUN", Instant.now(), 5L, 20L, false
		);

		List<ArticleResponse> sortedArticles = List.of(articleWithManyComments, articleWithFewComments);

		CursorPageResponseArticleDto mockResponse = CursorPageResponseArticleDto.builder()
			.content(sortedArticles)
			.hasNext(false)
			.size(10)
			.totalElements(2)
			.build();

		when(articleService.fetchArticles(any(ArticleRequest.class), any(UUID.class)))
			.thenReturn(mockResponse);

		mockMvc.perform(get("/api/articles?sortBy=COMMENT_COUNT")
				.header("Monew-Request-User-ID", "00000000-0000-0000-0000-000000000001"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].title").value("댓글 많은 기사"))
			.andExpect(jsonPath("$.content[0].commentCount").value(50));
	}
}
