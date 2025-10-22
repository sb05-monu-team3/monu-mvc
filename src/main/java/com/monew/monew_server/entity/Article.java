package com.monew.monew_server.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monew.monew_server.entity.common.BaseDeletableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "articles")
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseDeletableEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ArticleSource source;

	@Column(columnDefinition = "text", nullable = false)
	private String sourceUrl;

	@Column(columnDefinition = "text", nullable = false)
	private String title;

	@CreatedDate
	@Column(columnDefinition = "timestamp with time zone", nullable = false)
	private Instant publishDate;

	@Column(columnDefinition = "text")
	private String summary;
}
