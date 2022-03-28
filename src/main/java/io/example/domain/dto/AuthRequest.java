package io.example.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public record AuthRequest(
  @NotNull @Email String username,
  @NotNull String password) {

  public AuthRequest() {
    this(null, null);
  }
}
