package io.example.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SearchRequest<T>(
  @Valid
  @NotNull
  Page page,

  @Valid
  @NotNull
  T query
) {

  public SearchRequest {
  }

  public SearchRequest(T query) {
    this(new Page(), query);
  }
}
