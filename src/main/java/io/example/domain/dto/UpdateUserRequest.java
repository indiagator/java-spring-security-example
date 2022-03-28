package io.example.domain.dto;

import lombok.Builder;

import javax.validation.constraints.NotBlank;
import java.util.Set;

public record UpdateUserRequest(
  @NotBlank
  String fullName,
  Set<String> authorities
) {

  @Builder
  public UpdateUserRequest {
  }

  public UpdateUserRequest() {
    this(null, null);
  }
}
