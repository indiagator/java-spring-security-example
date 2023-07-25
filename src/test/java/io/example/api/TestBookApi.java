package io.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.AuthorTestDataFactory;
import io.example.api.data.BookTestDataFactory;
import io.example.domain.dto.AuthorView;
import io.example.domain.dto.BookView;
import io.example.domain.dto.EditBookRequest;
import io.example.domain.dto.ListResponse;
import io.example.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static io.example.util.JsonHelper.fromJson;
import static io.example.util.JsonHelper.toJson;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = Role.BOOK_ADMIN)
public class TestBookApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final AuthorTestDataFactory authorTestDataFactory;
  private final BookTestDataFactory bookTestDataFactory;

  @Autowired
  public TestBookApi(MockMvc mockMvc,
                     ObjectMapper objectMapper,
                     AuthorTestDataFactory authorTestDataFactory,
                     BookTestDataFactory bookTestDataFactory) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.authorTestDataFactory = authorTestDataFactory;
    this.bookTestDataFactory = bookTestDataFactory;
  }

  @Test
  public void testCreateSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");

    EditBookRequest goodRequest = new EditBookRequest(
      List.of(authorView.id()), "Test Book A", null, null, null, null, null, null, null, null
    );

    MvcResult createResult = this.mockMvc
      .perform(post("/api/book")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, goodRequest)))
      .andExpect(status().isOk())
      .andReturn();

    BookView bookView = fromJson(objectMapper, createResult.getResponse().getContentAsString(), BookView.class);
    assertNotNull(bookView.id(), "Book id must not be null!");
    assertEquals(goodRequest.title(), bookView.title(), "Book title update isn't applied!");
  }

  @Test
  public void testCreateFail() throws Exception {
    EditBookRequest badRequest = new EditBookRequest();

    this.mockMvc
      .perform(post("/api/book")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, badRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testEditSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");
    BookView bookView = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book A");

    EditBookRequest updateRequest = new EditBookRequest(
      null, "Test Book B", "Cool Book", null, null, null, null, null, null, null
    );

    MvcResult updateResult = this.mockMvc
      .perform(put(String.format("/api/book/%s", bookView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isOk())
      .andReturn();
    BookView newBookView = fromJson(objectMapper, updateResult.getResponse().getContentAsString(), BookView.class);

    assertEquals(updateRequest.title(), newBookView.title(), "Book title update isn't applied!");
    assertEquals(updateRequest.about(), newBookView.about(), "Book about update isn't applied!");
  }

  @Test
  public void testEditFailBadRequest() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");
    BookView bookView = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book A");

    EditBookRequest updateRequest = new EditBookRequest();

    this.mockMvc
      .perform(put(String.format("/api/book/%s", bookView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testEditFailNotFound() throws Exception {
    EditBookRequest updateRequest = new EditBookRequest(
      null, "Test Book B", null, null, null, null, null, null, null, null
    );

    this.mockMvc
      .perform(put(String.format("/api/book/%s", "5f07c259ffb98843e36a2aa9"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Book with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  public void testDeleteSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");
    BookView bookView = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book A");

    this.mockMvc
      .perform(delete(String.format("/api/book/%s", bookView.id())))
      .andExpect(status().isOk());

    this.mockMvc
      .perform(get(String.format("/api/book/%s", bookView.id())))
      .andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteFailNotFound() throws Exception {
    this.mockMvc
      .perform(delete(String.format("/api/book/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Book with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  @WithAnonymousUser
  public void testGetSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");
    BookView bookView = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book A");

    MvcResult getResult = this.mockMvc
      .perform(get(String.format("/api/book/%s", bookView.id())))
      .andExpect(status().isOk())
      .andReturn();

    BookView getBookView = fromJson(objectMapper, getResult.getResponse().getContentAsString(), BookView.class);

    assertEquals(bookView.id(), getBookView.id(), "Book ids must be equal!");
  }

  @Test
  @WithAnonymousUser
  public void testGetNotFound() throws Exception {
    this.mockMvc
      .perform(get(String.format("/api/book/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Book with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  @WithAnonymousUser
  public void testGetBookAuthorsSuccess() throws Exception {
    AuthorView authorView1 = authorTestDataFactory.createAuthor("Test Author A");
    AuthorView authorView2 = authorTestDataFactory.createAuthor("Test Author B");
    BookView bookView = bookTestDataFactory.createBook(List.of(authorView1.id(), authorView2.id()),
      "Test Book A");

    MvcResult getBooksResult = this.mockMvc
      .perform(get(String.format("/api/book/%s/author", bookView.id())))
      .andExpect(status().isOk())
      .andReturn();

    ListResponse<AuthorView> authorViewList = fromJson(objectMapper,
      getBooksResult.getResponse().getContentAsString(),
      new TypeReference<>() {
      });

    assertEquals(2, authorViewList.items().size(), "Book must have 2 authors");
    assertEquals(authorView1.fullName(), authorViewList.items().get(0).fullName(), "Author name mismatch!");
    assertEquals(authorView2.fullName(), authorViewList.items().get(1).fullName(), "Author name mismatch!");
  }

}
