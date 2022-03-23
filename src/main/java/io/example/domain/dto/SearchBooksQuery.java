package io.example.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;

public record SearchBooksQuery(
	String id,

	String creatorId,
	LocalDateTime createdAtStart,
	LocalDateTime createdAtEnd,

	String title,
	Set<String> genres,
	String isbn13,
	String isbn10,
	String publisher,
	LocalDate publishDateStart,
	LocalDate publishDateEnd,

	String authorId,
  String authorFullName
) {

	@Builder
  public SearchBooksQuery {}

  public SearchBooksQuery() {
    this(null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
}
