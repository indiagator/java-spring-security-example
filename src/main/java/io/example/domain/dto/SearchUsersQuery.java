package io.example.domain.dto;

import lombok.Builder;

public record SearchUsersQuery(
  String id,
  String username,
  String fullName
) {

  @Builder
  public SearchUsersQuery {
  }

  public SearchUsersQuery() {
    this(null, null, null);
  }
}
