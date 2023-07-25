package io.example.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Page(
  @Min(value = 1, message = "Paging must start with page 1") long number,
  @Min(value = 1, message = "You can request minimum 1 records")
  @Max(value = 100, message = "You can request maximum 100 records") long limit) {

  public Page() {
    this(1, 10);
  }
}
