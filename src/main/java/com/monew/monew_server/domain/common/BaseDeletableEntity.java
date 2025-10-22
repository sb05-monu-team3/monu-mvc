package com.monew.monew_server.domain.common;

import java.time.Instant;

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
public abstract class BaseDeletableEntity extends BaseUpdatableEntity {

	@Column(columnDefinition = "timestamp with time zone")
	private Instant deletedAt;

	public void softDelete() {
		this.deletedAt = Instant.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}