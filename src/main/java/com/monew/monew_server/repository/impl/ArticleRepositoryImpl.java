package com.monew.monew_server.repository.impl;

import static com.monew.monew_server.domain.article.entity.QArticle.*;
import static com.monew.monew_server.domain.article.entity.QArticleView.*;
import static com.monew.monew_server.domain.comment.entity.QComment.*;
import static com.monew.monew_server.domain.interest.entity.QArticleInterest.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.monew.monew_server.domain.article.entity.Article;
import com.monew.monew_server.domain.article.entity.ArticleSortType;
import com.monew.monew_server.domain.article.entity.ArticleSource;
import com.monew.monew_server.dto.ArticleRequest;
import com.monew.monew_server.repository.ArticleRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Article> findArticlesWithFilterAndCursor(ArticleRequest request, int size) {
		ArticleSortType sortBy = request.sortBy() == null ? ArticleSortType.DATE : request.sortBy();

		BooleanExpression cursorCondition = whereCursor(request, sortBy);
		BooleanBuilder commonCondition = whereCondition(request);

		// Query 시작
		JPQLQuery<Article> query = queryFactory.selectFrom(article)
			.leftJoin(articleView).on(articleView.article.eq(article))
			.where(cursorCondition, commonCondition)
			.groupBy(article.id);

		// 정렬
		OrderSpecifier<?> orderSpecifier;
		if (sortBy == ArticleSortType.VIEW_COUNT) {
			// 조회수 많은 순 + 최신 글 tie-breaker
			orderSpecifier = articleView.id.count().desc();
			query = query.orderBy(orderSpecifier, article.publishDate.desc(), article.id.desc());
		} else if (sortBy == ArticleSortType.COMMENT_COUNT) {
			orderSpecifier = getCountExpression(ArticleSortType.COMMENT_COUNT).desc();
			query = query.orderBy(orderSpecifier, article.publishDate.desc(), article.id.desc());
		} else {
			// 기본 날짜 정렬
			query = query.orderBy(article.publishDate.desc(), article.id.desc());
		}

		return query.limit(size).fetch();
	}

	@Override
	public long countArticlesWithFilter(ArticleRequest request) {
		BooleanBuilder condition = whereCondition(request);

		JPQLQuery<Long> query = queryFactory.select(article.id.countDistinct())
			.from(article)
			.where(condition);

		if (request.interestIds() != null && !request.interestIds().isEmpty()) {
			query.innerJoin(articleInterest).on(articleInterest.article.eq(article));
		}

		return query.fetchOne();
	}

	private BooleanBuilder whereCondition(ArticleRequest request) {
		BooleanBuilder builder = new BooleanBuilder();

		if (request.keyword() != null && !request.keyword().isBlank()) {
			builder.and(article.title.containsIgnoreCase(request.keyword())
				.or(article.summary.containsIgnoreCase(request.keyword())));
		}

		if (request.interestIds() != null && !request.interestIds().isEmpty()) {
			builder.and(articleInterest.interest.id.in(request.interestIds()));
		}

		if (request.source() != null) {
			try {
				ArticleSource source = ArticleSource.valueOf(request.source().toString());
				builder.and(article.source.eq(source));
			} catch (IllegalArgumentException e) {
				System.err.println("유효하지 않은 ArticleSource 값: " + request.source());
			}
		}

		if (request.date() != null) {
			LocalDate localDate = request.date();
			Instant startOfDay = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
			Instant endOfDay = localDate.atTime(23, 59, 59, 999999999).toInstant(ZoneOffset.UTC);
			builder.and(article.publishDate.between(startOfDay, endOfDay));
		}

		builder.and(article.deletedAt.isNull());

		return builder;
	}

	private BooleanExpression whereCursor(ArticleRequest request, ArticleSortType sortBy) {

		if (request.cursor() == null || request.cursor().isBlank() || request.nextAfter() == null || request.nextAfter()
			.isBlank()) {
			return null;
		}

		UUID nextCursorId;
		String nextAfterString = request.nextAfter();

		try {
			nextCursorId = UUID.fromString(request.cursor());

			if (sortBy == ArticleSortType.DATE) {
				Instant nextAfterInstant = Instant.parse(nextAfterString);

				BooleanExpression primarySort = article.publishDate.lt(nextAfterInstant);

				BooleanExpression tieBreaker = article.publishDate.eq(nextAfterInstant)
					.and(article.id.lt(nextCursorId));

				return primarySort.or(tieBreaker);

			} else if (sortBy == ArticleSortType.COMMENT_COUNT || sortBy == ArticleSortType.VIEW_COUNT) {

				Long nextAfterValue = Long.parseLong(nextAfterString);

				NumberExpression<Long> countExpression = getCountExpression(sortBy);

				BooleanExpression primarySort = countExpression.lt(nextAfterValue);

				BooleanExpression tieBreaker = countExpression.eq(nextAfterValue)
					.and(article.id.lt(nextCursorId));

				return primarySort.or(tieBreaker);
			}

		} catch (IllegalArgumentException | DateTimeParseException e) {
			System.err.println("커서 값 파싱 에러: " + e.getMessage());
			return article.id.isNull();
		}

		return null;
	}

	private OrderSpecifier<?> getOrderSpecifier(ArticleSortType sortBy) {

		if (sortBy == null) {
			sortBy = ArticleSortType.DATE;
		}

		switch (sortBy) {
			case COMMENT_COUNT:
			case VIEW_COUNT:
				return getCountExpression(sortBy).desc();

			case DATE:
			default:
				return article.publishDate.desc();
		}
	}

	private NumberExpression<Long> getCountExpression(ArticleSortType sortBy) {
		JPQLQuery<Long> countQuery;

		if (sortBy == ArticleSortType.COMMENT_COUNT) {
			countQuery = JPAExpressions.select(comment.id.count())
				.from(comment)
				.where(comment.article.id.eq(article.id).and(comment.deletedAt.isNull()));
		} else if (sortBy == ArticleSortType.VIEW_COUNT) {
			countQuery = JPAExpressions.select(articleView.id.count())
				.from(articleView)
				.where(articleView.article.id.eq(article.id));
		} else {
			return (NumberExpression<Long>)Expressions.constant(0L); // 기본값
		}

		return Expressions.numberTemplate(Long.class, "({0})", countQuery);
	}
}
