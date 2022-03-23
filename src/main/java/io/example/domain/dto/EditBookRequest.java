package io.example.domain.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Builder;

public record EditBookRequest(
		List<@NotNull String> authorIds,

		@NotNull String title,
		String about,
		String language,
		List<String> genres,
		String isbn13,
		String isbn10,
		String publisher,
		LocalDate publishDate,
		Integer hardcover) {

	@Builder
	public EditBookRequest {
	}

	public EditBookRequest() {
		this(null, null, null, null, null, null, null, null, null, null);
	}
}
