package io.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.AuthorTestDataFactory;
import io.example.api.data.BookTestDataFactory;
import io.example.domain.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static io.example.util.JsonHelper.fromJson;
import static io.example.util.JsonHelper.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestBookSearchApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final AuthorTestDataFactory authorTestDataFactory;
  private final BookTestDataFactory bookTestDataFactory;

  @Autowired
  public TestBookSearchApi(MockMvc mockMvc,
                           ObjectMapper objectMapper,
                           AuthorTestDataFactory authorTestDataFactory,
                           BookTestDataFactory bookTestDataFactory) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.authorTestDataFactory = authorTestDataFactory;
    this.bookTestDataFactory = bookTestDataFactory;
  }

  @Test
  public void testSearch() throws Exception {
    AuthorView author1 = authorTestDataFactory.createAuthor("Book Search A Author");
    AuthorView author2 = authorTestDataFactory.createAuthor("Book Search B Author");
    AuthorView author3 = authorTestDataFactory.createAuthor("Book Search C Author");

    List<String> authorIds1 = List.of(author1.id(), author2.id());
    List<String> authorIds2 = List.of(author3.id());

    BookView book1 = bookTestDataFactory.createBook(authorIds1, "Book Search A Book", null, null,
      List.of("Book Search Genre A", "Book Search Genre B"));
    BookView book2 = bookTestDataFactory.createBook(authorIds1, "Book Search B Book", null, null, null,
      "978-1-56619-909-4");
    BookView book3 = bookTestDataFactory.createBook(authorIds1, "Book Search C Book", null, null, null, null,
      "1-56619-909-3");
    BookView book4 = bookTestDataFactory.createBook(authorIds1, "Book Search D Book", null, null, null, null, null,
      "Book Search A Publisher");
    BookView book5 = bookTestDataFactory.createBook(authorIds1, "Book Search E Book", null, null, null, null, null,
      null, LocalDate.of(1985, 7, 17));
    BookView book6 = bookTestDataFactory.createBook(authorIds2, "Book Search F Book");
    BookView book7 = bookTestDataFactory.createBook(authorIds2, "Book Search G Book");
    BookView book8 = bookTestDataFactory.createBook(authorIds2, "Book Search H Book");
    BookView book9 = bookTestDataFactory.createBook(authorIds2, "Book Search I Book");
    BookView book10 = bookTestDataFactory.createBook(authorIds2, "Book Search J Book");

    testIdFilter(book1.id());
    testTitleFilter();
    testGenresFilter();
    testIsbn13Filter();
    testIsbn10Filter();
    testPublisherFilter();
    testPublishDateFilter();
    testAuthorIdFilter(author1.id());
    testAuthorFullNameFilter();

    bookTestDataFactory.deleteBook(book1.id());
    bookTestDataFactory.deleteBook(book2.id());
    bookTestDataFactory.deleteBook(book3.id());
    bookTestDataFactory.deleteBook(book4.id());
    bookTestDataFactory.deleteBook(book5.id());
    bookTestDataFactory.deleteBook(book6.id());
    bookTestDataFactory.deleteBook(book7.id());
    bookTestDataFactory.deleteBook(book8.id());
    bookTestDataFactory.deleteBook(book9.id());
    bookTestDataFactory.deleteBook(book10.id());

    authorTestDataFactory.deleteAuthor(author1.id());
    authorTestDataFactory.deleteAuthor(author2.id());
    authorTestDataFactory.deleteAuthor(author3.id());
  }

  private void testIdFilter(String id) throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query with book id equal
    query = SearchBooksQuery.builder().id(id).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testTitleFilter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query book title contains
    query = SearchBooksQuery.builder().title("Book Search G").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");

    // Search query book title contains case insensitive
    query = SearchBooksQuery.builder().title("Book Search g").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testGenresFilter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query genres all
    query = SearchBooksQuery.builder().genres(Set.of("Book Search Genre A", "Book Search Genre B")).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");

    // Search query genres mismatch
    query = SearchBooksQuery.builder().genres(Set.of("Book Search Genre A", "Book Search Genre C")).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(0, bookViewList.items().size(), "Invalid search result!");

    // Search query genres partial
    query = SearchBooksQuery.builder().genres(Set.of("Book Search Genre A")).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testIsbn13Filter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query isbn13 equals
    query = SearchBooksQuery.builder().isbn13("978-1-56619-909-4").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testIsbn10Filter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query isbn10 equals
    query = SearchBooksQuery.builder().isbn10("1-56619-909-3").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testPublisherFilter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query book publisher contains
    query = SearchBooksQuery.builder().publisher("Book Search A Pub").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");

    // Search query book publisher contains case insensitive
    query = SearchBooksQuery.builder().publisher("Book Search a").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");
  }

  private void testPublishDateFilter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query publish date interval contains
    query = SearchBooksQuery.builder().publishDateStart(LocalDate.of(1985, 5, 1)).publishDateEnd(LocalDate.of(1985, 9, 1)).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");

    // Search query publish date interval not contains
    query = SearchBooksQuery.builder().publishDateStart(LocalDate.of(1985, 8, 1)).publishDateEnd(LocalDate.of(1985, 9, 1)).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(0, bookViewList.items().size(), "Invalid search result!");

    // Search query publish date interval start is inclusive
    query = SearchBooksQuery.builder().publishDateStart(LocalDate.of(1985, 7, 17)).publishDateEnd(LocalDate.of(1985, 9, 1)).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(1, bookViewList.items().size(), "Invalid search result!");

    // Search query publish date interval end is exclusive
    query = SearchBooksQuery.builder().publishDateStart(LocalDate.of(1985, 5, 1)).publishDateEnd(LocalDate.of(1985, 7, 17)).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(0, bookViewList.items().size(), "Invalid search result!");
  }

  private void testAuthorIdFilter(String authorId) throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query with book id equal
    query = SearchBooksQuery.builder().authorId(authorId).build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(5, bookViewList.items().size(), "Invalid search result!");
  }

  private void testAuthorFullNameFilter() throws Exception {
    SearchBooksQuery query;
    ListResponse<BookView> bookViewList;

    // Search query author full name contains
    query = SearchBooksQuery.builder().authorFullName("Book Search A").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(5, bookViewList.items().size(), "Invalid search result!");

    // Search query author full name contains case insensitive
    query = SearchBooksQuery.builder().authorFullName("Book Search c").build();
    bookViewList = execute("/api/book/search", query);
    assertEquals(5, bookViewList.items().size(), "Invalid search result!");
  }

  private ListResponse<BookView> execute(String url, SearchBooksQuery query) throws Exception {
    MvcResult result = this.mockMvc
      .perform(post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, new SearchRequest<>(query))))
      .andExpect(status().isOk())
      .andReturn();

    return fromJson(objectMapper,
      result.getResponse().getContentAsString(),
      new TypeReference<>() {
      });
  }

}
