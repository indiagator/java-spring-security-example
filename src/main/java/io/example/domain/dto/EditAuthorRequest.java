package io.example.domain.dto;

import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import java.util.List;

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
