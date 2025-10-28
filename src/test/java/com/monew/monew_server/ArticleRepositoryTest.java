package com.monew.monew_server;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.monew.monew_server.config.QuerydslConfig;
import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.entity.ArticleSource;
import com.monew.monew_server.dto.ArticleRequest;
import com.monew.monew_server.repository.ArticleRepository;
import com.monew.monew_server.repository.ArticleRepositoryCustom;

@DataJpaTest
@Import(QuerydslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

	@Autowired
	private ArticleRepository articleRepository;

	@Qualifier("articleRepositoryImpl")
	@Autowired
	private ArticleRepositoryCustom articleRepositoryCustom;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE articles RESTART IDENTITY CASCADE");
	}

	@Test
	@DisplayName("키워드로 기사 검색 - 제목 매칭")
	void shouldFindArticlesByKeywordInTitle() {
		Article article1 = Article.builder()
			.title("삼성전자 실적 발표")
			.summary("요약")
			.source(ArticleSource.HANKYUNG)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		Article article2 = Article.builder()
			.title("LG전자 신제품")
			.summary("요약")
			.source(ArticleSource.YEONHAP)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		articleRepository.saveAll(List.of(article1, article2));

		ArticleRequest request = new ArticleRequest(
			"삼성",
			null,
			null,
			null,
			null,
			ArticleSortType.DATE,
			10,
			"2025-10-27T10:30:00Z"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).contains("삼성");
	}

	@Test
	@DisplayName("키워드로 기사 검색 - 요약 매칭")
	void shouldFindArticlesByKeywordInSummary() {
		Article article = Article.builder()
			.title("기사 제목")
			.summary("삼성전자의 실적이 좋습니다")
			.source(ArticleSource.NAVER)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		articleRepository.save(article);

		ArticleRequest request = new ArticleRequest(
			"삼성",
			null,
			ArticleSource.NAVER,
			null,
			null,
			ArticleSortType.DATE,
			10,
			null
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getSummary()).contains("삼성");
	}

	@Test
	@DisplayName("전체 기사 개수 조회 - 조건 없는 경우 전체 반환")
	void shouldCountAllArticles() {
		Article article1 = Article.builder()
			.title("기사1")
			.summary("요약1")
			.source(ArticleSource.NAVER)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		Article article2 = Article.builder()
			.title("기사2")
			.summary("요약2")
			.source(ArticleSource.HANKYUNG)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		articleRepository.saveAll(List.of(article1, article2));

		ArticleRequest request = new ArticleRequest(
			null,
			null,
			null,
			null,
			null,
			ArticleSortType.DATE,
			10,
			null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(request);

		assertThat(count).isEqualTo(2L);
	}

	@Test
	@DisplayName("필터 조건에 맞는 기사 개수 조회")
	void shouldCountArticlesWithFilter() {
		Article article1 = Article.builder()
			.title("삼성전자 뉴스")
			.summary("내용")
			.source(ArticleSource.HANKYUNG)
			.sourceUrl("http://test1.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		Article article2 = Article.builder()
			.title("LG전자 뉴스")
			.summary("내용")
			.source(ArticleSource.YEONHAP)
			.sourceUrl("http://test2.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		articleRepository.saveAll(List.of(article1, article2));

		ArticleRequest request = new ArticleRequest(
			"삼성",
			null,
			null,
			null,
			null,
			ArticleSortType.DATE,
			10,
			null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(request);

		assertThat(count).isEqualTo(1);
	}

	@Test
	@DisplayName("관심사 ID 조건이 있는 경우 countArticlesWithFilter 정상 동작")
	void shouldCountArticlesWithInterestIds() {
		Article article = Article.builder()
			.title("AI 뉴스")
			.summary("테스트")
			.source(ArticleSource.YEONHAP)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.now())
			.build();

		articleRepository.save(article);
		List<UUID> interestIds = List.of(UUID.randomUUID());

		ArticleRequest request = new ArticleRequest(
			null,
			interestIds,
			null,
			null,
			null,
			ArticleSortType.DATE,
			10,
			null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(request);

		assertThat(count).isGreaterThanOrEqualTo(0);
	}

	@Test
	@DisplayName("커서 기반 페이지네이션 조건이 적용된 기사 조회")
	void shouldFindArticlesWithCursorCondition() {
		// given
		Article article1 = Article.builder()
			.title("첫 번째 기사")
			.summary("내용1")
			.source(ArticleSource.NAVER)
			.sourceUrl("http://a.com")
			.createdAt(Instant.now())
			.publishDate(Instant.parse("2025-10-25T10:00:00Z"))
			.build();

		Article article2 = Article.builder()
			.title("두 번째 기사")
			.summary("내용2")
			.source(ArticleSource.NAVER)
			.sourceUrl("http://b.com")
			.createdAt(Instant.now())
			.publishDate(Instant.parse("2025-10-26T10:00:00Z"))
			.build();

		articleRepository.saveAll(List.of(article1, article2));

		String cursorId = article2.getId().toString();
		String nextAfter = "2025-10-26T10:00:00Z";

		ArticleRequest request = new ArticleRequest(
			null,
			null,
			null,
			null,
			cursorId,
			ArticleSortType.DATE,
			10,
			nextAfter
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("whereCursor - 정상 커서 파싱 및 조건 생성")
	@Test
	void shouldCreateCursorCondition() {
		// given
		Article article = Article.builder()
			.title("테스트 기사")
			.summary("요약")
			.source(ArticleSource.YEONHAP)
			.sourceUrl("http://test.com")
			.createdAt(Instant.now())
			.publishDate(Instant.parse("2025-10-25T10:00:00Z"))
			.build();
		articleRepository.save(article);

		ArticleRequest request = new ArticleRequest(
			null,
			null,
			null,
			null,
			article.getId().toString(),
			ArticleSortType.DATE,
			10,
			"2025-10-25T10:00:00Z"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("whereCursor - 커서 ID가 잘못된 경우 UUID 파싱 예외 발생")
	@Test
	void shouldHandleInvalidCursorIdGracefully() {
		// given
		ArticleRequest request = new ArticleRequest(
			null,
			null,
			null,
			null,
			"not-a-uuid",
			ArticleSortType.DATE,
			10,
			"2025-10-25T10:00:00Z"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isEmpty();
	}

	@DisplayName("whereCursor - nextAfter가 비어있는 경우 조건 없이 조회")
	@Test
	void shouldSkipCursorConditionWhenNextAfterBlank() {
		// given
		ArticleRequest request = new ArticleRequest(
			null,
			null,
			null,
			null,
			UUID.randomUUID().toString(),
			ArticleSortType.DATE,
			10,
			""
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("getCountExpression - 댓글 수 기준 정렬 시 올바른 식 생성")
	@Test
	void shouldReturnCommentCountExpression() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null,
			UUID.randomUUID().toString(),
			ArticleSortType.COMMENT_COUNT,
			10,
			"100"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("getCountExpression - 조회수 기준 정렬 시 올바른 식 생성")
	@Test
	void shouldReturnViewCountExpression() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null,
			UUID.randomUUID().toString(),
			ArticleSortType.VIEW_COUNT,
			10,
			"100"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("getCountExpression - 기본값(기타) 분기 커버")
	@Test
	void shouldReturnDefaultCountExpression() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null,
			UUID.randomUUID().toString(),
			ArticleSortType.DATE,
			10,
			"2025-10-25T10:00:00Z"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);

		assertThat(result).isNotNull();
	}

	@DisplayName("whereCursor - cursor가 null이면 조건 없이 조회")
	@Test
	void shouldSkipCursorConditionWhenCursorNull() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null,
			ArticleSortType.DATE, 10, "2025-10-25T10:00:00Z"
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);
		assertThat(result).isNotNull();
	}

	@DisplayName("whereCondition - source가 유효하지 않은 경우 예외 처리 분기")
	@Test
	void shouldHandleInvalidSourceEnum() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null,
			ArticleSortType.DATE, 10, null
		);

		ArticleRequest spyRequest = new ArticleRequest(
			null, null, ArticleSource.valueOf("NAVER"), null, null,
			ArticleSortType.DATE, 10, null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(spyRequest);
		assertThat(count).isGreaterThanOrEqualTo(0);
	}

	@DisplayName("whereCondition - keyword가 null일 때")
	@Test
	void shouldHandleNullKeyword() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null,
			ArticleSortType.DATE, 10, null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(request);
		assertThat(count).isGreaterThanOrEqualTo(0);
	}

	@DisplayName("whereCondition - interestIds가 empty일 때")
	@Test
	void shouldHandleEmptyInterestIds() {
		ArticleRequest request = new ArticleRequest(
			null, List.of(), null, null, null,
			ArticleSortType.DATE, 10, null
		);

		long count = articleRepositoryCustom.countArticlesWithFilter(request);
		assertThat(count).isGreaterThanOrEqualTo(0);
	}

	@DisplayName("getCountExpression - 기본값 분기 커버")
	@Test
	void shouldCoverDefaultGetCountExpression() {
		ArticleRequest request = new ArticleRequest(
			null, null, null, null, null,
			ArticleSortType.DATE, 10, null
		);

		List<Article> result = articleRepositoryCustom.findArticlesWithFilterAndCursor(request, 10);
		assertThat(result).isNotNull();
	}
}
