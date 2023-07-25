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

import java.util.List;
import java.util.Set;

import static io.example.util.JsonHelper.fromJson;
import static io.example.util.JsonHelper.toJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestAuthorSearchApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final AuthorTestDataFactory authorTestDataFactory;
  private final BookTestDataFactory bookTestDataFactory;

  @Autowired
  public TestAuthorSearchApi(MockMvc mockMvc,
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
    AuthorView author1 = authorTestDataFactory.createAuthor("Author Search A Author", null, null,
      List.of("Author Search Genre A", "Author Search Genre B"));
    AuthorView author2 = authorTestDataFactory.createAuthor("Author Search B Author");
    AuthorView author3 = authorTestDataFactory.createAuthor("Author Search C Author");
    AuthorView author4 = authorTestDataFactory.createAuthor("Author Search D Author");
    AuthorView author5 = authorTestDataFactory.createAuthor("Author Search E Author");

    List<String> authorIds1 = List.of(author1.id(), author2.id(), author3.id());
    List<String> authorIds2 = List.of(author4.id(), author5.id());

    BookView book1 = bookTestDataFactory.createBook(authorIds1, "Author Search A Book");
    BookView book2 = bookTestDataFactory.createBook(authorIds1, "Author Search B Book");
    BookView book3 = bookTestDataFactory.createBook(authorIds1, "Author Search C Book");
    BookView book4 = bookTestDataFactory.createBook(authorIds2, "Author Search D Book");
    BookView book5 = bookTestDataFactory.createBook(authorIds2, "Author Search E Book");

    testIdFilter(author1.id());
    testFullNameFilter();
    testGenresFilter();
    testBookIdFilter(book1.id());
    testBookTitleFilter();

    bookTestDataFactory.deleteBook(book1.id());
    bookTestDataFactory.deleteBook(book2.id());
    bookTestDataFactory.deleteBook(book3.id());
    bookTestDataFactory.deleteBook(book4.id());
    bookTestDataFactory.deleteBook(book5.id());

    authorTestDataFactory.deleteAuthor(author1.id());
    authorTestDataFactory.deleteAuthor(author2.id());
    authorTestDataFactory.deleteAuthor(author3.id());
    authorTestDataFactory.deleteAuthor(author4.id());
    authorTestDataFactory.deleteAuthor(author5.id());
  }

  private void testIdFilter(String id) throws Exception {
    SearchAuthorsQuery query;
    ListResponse<AuthorView> authorViewList;

    // Search query with book id equal
    query = SearchAuthorsQuery.builder().id(id).build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(1, authorViewList.items().size(), "Invalid search result!");
  }

  private void testFullNameFilter() throws Exception {
    SearchAuthorsQuery query;
    ListResponse<AuthorView> authorViewList;

    // Search query author full name contains
    query = SearchAuthorsQuery.builder().fullName("Author Search A").build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(1, authorViewList.items().size(), "Invalid search result!");

    // Search query author full name contains case insensitive
    query = SearchAuthorsQuery.builder().fullName("Author Search b").build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(1, authorViewList.items().size(), "Invalid search result!");
  }

  private void testGenresFilter() throws Exception {
    SearchAuthorsQuery query;
    ListResponse<AuthorView> authorViewList;

    // Search query genres all
    query = SearchAuthorsQuery.builder().genres(Set.of("Author Search Genre A", "Author Search Genre B")).build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(1, authorViewList.items().size(), "Invalid search result!");

    // Search query genres mismatch
    query = SearchAuthorsQuery.builder().genres(Set.of("Author Search Genre A", "Author Search Genre C")).build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(0, authorViewList.items().size(), "Invalid search result!");

    // Search query genres partial
    query = SearchAuthorsQuery.builder().genres(Set.of("Author Search Genre A")).build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(1, authorViewList.items().size(), "Invalid search result!");
  }

  private void testBookIdFilter(String bookId) throws Exception {
    SearchAuthorsQuery query;
    ListResponse<AuthorView> authorViewList;

    // Search query with book id equal
    query = SearchAuthorsQuery.builder().bookId(bookId).build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(3, authorViewList.items().size(), "Invalid search result!");
  }

  private void testBookTitleFilter() throws Exception {
    SearchAuthorsQuery query;
    ListResponse<AuthorView> authorViewList;

    // Search query book title contains
    query = SearchAuthorsQuery.builder().bookTitle("Author Search A").build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(3, authorViewList.items().size(), "Invalid search result!");

    // Search query book title contains case insensitive
    query = SearchAuthorsQuery.builder().bookTitle("Author Search c").build();
    authorViewList = execute("/api/author/search", query);
    assertEquals(3, authorViewList.items().size(), "Invalid search result!");
  }

  private ListResponse<AuthorView> execute(String url, SearchAuthorsQuery query) throws Exception {
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
