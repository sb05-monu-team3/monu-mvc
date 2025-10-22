package com.monew.monew_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new io.swagger.v3.oas.models.info.Info()
				.title("Monew Server API")
				.version("v1.0.0")
				.description("Monew Server Swagger API 문서입니다.")
			).servers(List.of(
				new Server()
					.url("http://localhost:8080")
					.description("로컬 모뉴 서버"),
				new Server()
					.url("http://localhost:8081")
					.description("로컬 모뉴 액츄에이터 서버")
			));
	}
}
