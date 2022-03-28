package io.example.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

public record CreateUserRequest(
  @NotBlank @Email String username,
  @NotBlank String fullName,
  @NotBlank String password,
  @NotBlank String rePassword,
  Set<String> authorities) {

  public CreateUserRequest {
    if (authorities == null) {
      authorities = new HashSet<>();
    }
  }

  public CreateUserRequest(
    String username,
    String fullName,
    String password,
    String rePassword
  ) {
    this(username, fullName, password, rePassword, new HashSet<>());
  }

  public CreateUserRequest(
    String username,
    String fullName,
    String password
  ) {
    this(username, fullName, password, password, new HashSet<>());
  }
}
