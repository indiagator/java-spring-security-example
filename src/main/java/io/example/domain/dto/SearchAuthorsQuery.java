package io.example.domain.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;

public record SearchAuthorsQuery(
	String id,

	String creatorId,
	LocalDateTime createdAtStart,
	LocalDateTime createdAtEnd,

	String fullName,
	Set<String> genres,

	String bookId,
	String bookTitle
) {

	@Builder
  public SearchAuthorsQuery {}

  public SearchAuthorsQuery() {
    this(null, null, null, null, null, null, null, null);
  }
}
