package com.monew.monew_server.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monew.monew_server.entity.common.BaseDeletableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDeletableEntity {

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, length = 30)
	private String nickname;

	@Column(nullable = false, length = 60)
	private String password;
}
