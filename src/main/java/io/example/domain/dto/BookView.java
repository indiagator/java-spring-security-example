package io.example.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookView(
  String id,

  UserView creator,
  LocalDateTime createdAt,

  String title,
  String about,
  String language,
  List<String> genres,
  String isbn13,
  String isbn10,
  String publisher,
  LocalDate publishDate,
  int hardcover) {

}
