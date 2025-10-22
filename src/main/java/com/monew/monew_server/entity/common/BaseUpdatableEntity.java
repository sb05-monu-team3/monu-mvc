package com.monew.monew_server.entity.common;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUpdatableEntity extends BaseEntity {

	@LastModifiedDate
	@Column(columnDefinition = "timestamp with time zone default now()")
	private Instant updatedAt;
}