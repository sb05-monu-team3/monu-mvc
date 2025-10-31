package com.monew.monew_server.domain.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.monew.monew_server.domain.article.dto.ArticleRequest;
import com.monew.monew_server.domain.article.dto.ArticleResponse;
import com.monew.monew_server.domain.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.entity.ArticleSource;
import com.monew.monew_server.domain.article.mapper.ArticleMapper;
import com.monew.monew_server.domain.article.repository.ArticleRepositoryCustom;
import com.monew.monew_server.domain.article.repository.ArticleViewRepository;
import com.monew.monew_server.domain.article.repository.projection.CommentCountProjection;
import com.monew.monew_server.domain.article.repository.projection.ViewCountProjection;
import com.monew.monew_server.domain.comment.repository.CommentRepository;
import com.monew.monew_server.exception.ArticleNotFoundException;

class ArticleServiceTest {

	@Mock
	private ArticleRepositoryCustom articleRepositoryCustom;
	@Mock
	private ArticleMapper articleMapper;
	@Mock
	private ArticleViewRepository articleViewRepository;
	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private ArticleService articleService;

	private final UUID DUMMY_USER_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(articleViewRepository.findViewCountsByArticleIds(anyList())).thenReturn(Collections.emptyList());
		when(articleViewRepository.findArticleIdsViewedByUser(anyList(), any())).thenReturn(Collections.emptySet());
		when(commentRepository.findCommentCountsByArticleIds(anyList())).thenReturn(Collections.emptyList());
	}

	@Test
	@DisplayName("Service - 키워드로 기사 검색 및 hasNext true")
	void shouldReturnArticlesByKeyword() {
		ArticleRequest request = new ArticleRequest("삼성", null, ArticleSource.NAVER, null, null, ArticleSortType.DATE,
			10, null);
		List<Article> mockArticles = IntStream.range(0, 11)
			.mapToObj((int i) -> Article.builder()
				.id(UUID.randomUUID())
				.title("삼성전자 뉴스 " + i)
				.summary("내용 " + i)
				.source(ArticleSource.NAVER)
				.sourceUrl("http://test.com/" + i)
				.createdAt(Instant.now().plus(i, ChronoUnit.MINUTES))
				.publishDate(Instant.now().plus(i, ChronoUnit.MINUTES))
				.build())
			.collect(Collectors.toList());

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 11)).thenReturn(mockArticles);
		when(articleMapper.toResponseList(anyList())).thenReturn(mockArticles.stream()
			.map(a -> new ArticleResponse(a.getId(), a.getTitle(), a.getSummary(), a.getSourceUrl(), a.getPublishDate(),
				0L, 0L, false))
			.collect(Collectors.toList()));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(100L);

		CursorPageResponseArticleDto response = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(response.getContent()).hasSize(10);
		assertThat(response.isHasNext()).isTrue();
		assertThat(response.getNextCursor()).isEqualTo(mockArticles.get(10).getId().toString());
		assertThat(response.getNextAfter()).isEqualTo(mockArticles.get(10).getPublishDate().toString());

		verify(articleRepositoryCustom, times(1)).findArticlesWithFilterAndCursor(request, 11);
	}

	@Test
	@DisplayName("Service - 키워드 검색 결과가 0개일 때 ArticleNotFoundException 발생")
	void shouldThrowExceptionWhenKeywordSearchHasNoResults() {
		ArticleRequest request = new ArticleRequest("없는키워드", null, null, null, null, ArticleSortType.DATE, 10, null);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 11)).thenReturn(Collections.emptyList());
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(0L);

		assertThatThrownBy(() -> articleService.fetchArticles(request, DUMMY_USER_ID))
			.isInstanceOf(ArticleNotFoundException.class)
			.hasMessage("ARTICLE_NOT_FOUND");
	}

	@Test
	@DisplayName("Service - sortBy null일 때 DATE 정렬을 기본값으로 사용")
	void shouldUseDateSortAsDefault() {
		Instant publishDate = Instant.now();
		Article a1 = Article.builder().id(UUID.randomUUID()).publishDate(publishDate).build();
		Article a2 = Article.builder().id(UUID.randomUUID()).publishDate(publishDate.minusSeconds(1)).build();

		ArticleRequest request = new ArticleRequest(null, null, null, null, null, null, 1, null); // sortBy=null

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(any(ArticleRequest.class), eq(2)))
			.thenReturn(List.of(a1, a2));
		when(articleRepositoryCustom.countArticlesWithFilter(any(ArticleRequest.class))).thenReturn(2L);
		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(a1.getId(), "A1", "S", "url", a1.getPublishDate(), 0L, 0L, false),
			new ArticleResponse(a2.getId(), "A2", "S", "url", a2.getPublishDate(), 0L, 0L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isTrue();
		assertThat(dto.getNextAfter()).isEqualTo(a2.getPublishDate().toString());
		assertThat(dto.getNextCursor()).isEqualTo(a2.getId().toString());
	}

	@Test
	@DisplayName("Service - sortBy VIEW_COUNT 처리 및 hasNext true")
	void shouldHandleViewCountSortWithHasNext() {
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		Article a1 = Article.builder().id(id1).publishDate(Instant.now()).build(); // 조회수 10
		Article a2 = Article.builder().id(id2).publishDate(Instant.now().plusSeconds(1)).build();

		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null, ArticleSortType.VIEW_COUNT, 1, null
		);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 2))
			.thenReturn(List.of(a1, a2));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(2L);

		when(articleViewRepository.findViewCountsByArticleIds(anyList())).thenReturn(List.of(
			new ViewCountProjection() {
				@Override
				public UUID getArticleId() {
					return id1;
				}

				@Override
				public long getViewCount() {
					return 10L;
				}
			},
			new ViewCountProjection() {
				@Override
				public UUID getArticleId() {
					return id2;
				}

				@Override
				public long getViewCount() {
					return 5L;
				}
			}
		));

		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(id1, "A1", "S1", "url", Instant.now(), 0L, 10L, false),
			new ArticleResponse(id2, "A2", "S2", "url", Instant.now().plusSeconds(1), 0L, 5L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isTrue();
		assertThat(dto.getNextAfter()).isEqualTo("5");
		assertThat(dto.getNextCursor()).isEqualTo(id2.toString());
	}

	@Test
	@DisplayName("Service - sortBy COMMENT_COUNT 처리 및 hasNext false")
	void shouldHandleCommentCountSortWithNoNextPage() {
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		Article a1 = Article.builder().id(id1).publishDate(Instant.now()).build(); // 댓글수 3
		Article a2 = Article.builder().id(id2).publishDate(Instant.now().plusSeconds(1)).build(); // 댓글수 3

		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null, ArticleSortType.COMMENT_COUNT, 2, null
		);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 3))
			.thenReturn(List.of(a1, a2));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(2L);

		when(commentRepository.findCommentCountsByArticleIds(anyList())).thenReturn(List.of(
			new CommentCountProjection() {
				@Override
				public UUID getArticleId() {
					return id1;
				}

				@Override
				public Long getCommentCount() {
					return 3L;
				}
			},
			new CommentCountProjection() {
				@Override
				public UUID getArticleId() {
					return id2;
				}

				@Override
				public Long getCommentCount() {
					return 3L;
				}
			}
		));

		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(id1, "A1", "S1", "url", Instant.now(), 3L, 0L, false),
			new ArticleResponse(id2, "A2", "S2", "url", Instant.now().plusSeconds(1), 3L, 0L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isFalse();
		assertThat(dto.getNextAfter()).isEqualTo("3");
		assertThat(dto.getNextCursor()).isNull();
		assertThat(dto.getSize()).isEqualTo(2);
	}

	@Test
	@DisplayName("Service - sortBy VIEW_COUNT 처리 + size null 기본값 확인")
	void shouldHandleViewCountSortAndDefaultPageSize() {
		Article a1 = Article.builder().id(UUID.randomUUID()).title("A1").publishDate(Instant.now()).build();
		Article a2 = Article.builder().id(UUID.randomUUID()).title("A2").publishDate(Instant.now()).build();

		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null, ArticleSortType.VIEW_COUNT, null, null
		);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 11))
			.thenReturn(List.of(a1, a2));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(2L);

		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(a1.getId(), "A1", "S", "url", Instant.now(), 0L, 5L, false),
			new ArticleResponse(a2.getId(), "A2", "S", "url", Instant.now(), 0L, 5L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isFalse();
		assertThat(dto.getNextAfter()).isEqualTo("5");
		assertThat(dto.getSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("Service - sortBy DATE 처리 및 hasNext false 케이스 확인")
	void shouldHandleDateSortWithNoNextPage() {
		Article a1 = Article.builder().id(UUID.randomUUID()).title("A1").publishDate(Instant.now()).build();

		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null, ArticleSortType.DATE, 1, null
		);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 2))
			.thenReturn(List.of(a1));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(1L);

		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(a1.getId(), "A1", "S", "url", Instant.now(), 0L, 0L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isFalse();
		assertThat(dto.getNextAfter()).isNull();
		assertThat(dto.getNextCursor()).isNull();
		assertThat(dto.getSize()).isEqualTo(1);
	}

	@Test
	@DisplayName("Service - hasNext true + sortBy COMMENT_COUNT 처리")
	void shouldHandleHasNextAndCommentCountSort() {
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		Article a1 = Article.builder().id(id1).title("A1").publishDate(Instant.now()).build();
		Article a2 = Article.builder().id(id2).title("A2").publishDate(Instant.now()).build();

		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null, ArticleSortType.COMMENT_COUNT, 1, null
		);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 2))
			.thenReturn(List.of(a1, a2));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(2L);

		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(id1, "A1", "S1", "url", Instant.now(), 1L, 2L, false),
			new ArticleResponse(id2, "A2", "S2", "url", Instant.now(), 1L, 2L, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.isHasNext()).isTrue();
		assertThat(dto.getNextAfter()).isEqualTo("1");
		assertThat(dto.getNextCursor()).isEqualTo(id2.toString());
		assertThat(dto.getContent()).hasSize(1);
	}

	@Test
	@DisplayName("Service - viewCount/commentCount null 처리 시 기본값 0 적용")
	void shouldHandleNullViewAndCommentCounts() {
		Article article = Article.builder().id(UUID.randomUUID()).publishDate(Instant.now()).build();
		ArticleRequest request = new ArticleRequest(null, null, null, null, null, ArticleSortType.VIEW_COUNT, 1, null);

		when(articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 2))
			.thenReturn(List.of(article));
		when(articleRepositoryCustom.countArticlesWithFilter(request)).thenReturn(1L);

		when(articleViewRepository.findViewCountsByArticleIds(anyList())).thenReturn(Collections.emptyList());
		when(commentRepository.findCommentCountsByArticleIds(anyList())).thenReturn(Collections.emptyList());
		when(articleMapper.toResponseList(anyList())).thenReturn(List.of(
			new ArticleResponse(article.getId(), "A", "S", "url", Instant.now(), null, null, false)
		));

		CursorPageResponseArticleDto dto = articleService.fetchArticles(request, DUMMY_USER_ID);

		assertThat(dto.getContent().get(0).viewCount()).isEqualTo(0L);
		assertThat(dto.getContent().get(0).commentCount()).isEqualTo(0L);
	}
}
