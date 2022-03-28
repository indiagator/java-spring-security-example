package io.example.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AuthorView(
  String id,

  UserView creator,
  LocalDateTime createdAt,

  String fullName,
  String about,
  String nationality,
  List<String> genres
) {

}
