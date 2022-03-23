package io.example.domain.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Builder;

public record EditAuthorRequest(
		@NotNull String fullName,
		String about,
		String nationality,
		List<String> genres) {

	@Builder
	public EditAuthorRequest {
	}

	public EditAuthorRequest() {
		this(null, null, null, null);
	}
}
