package io.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.AuthorTestDataFactory;
import io.example.api.data.BookTestDataFactory;
import io.example.domain.dto.AuthorView;
import io.example.domain.dto.BookView;
import io.example.domain.dto.EditAuthorRequest;
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
@WithMockUser(roles = Role.AUTHOR_ADMIN)
public class TestAuthorApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final AuthorTestDataFactory authorTestDataFactory;
  private final BookTestDataFactory bookTestDataFactory;

  @Autowired
  public TestAuthorApi(MockMvc mockMvc,
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
    EditAuthorRequest goodRequest = EditAuthorRequest.builder().fullName("Test Author A").build();

    MvcResult createResult = this.mockMvc
      .perform(post("/api/author")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, goodRequest)))
      .andExpect(status().isOk())
      .andReturn();

    AuthorView authorView = fromJson(objectMapper, createResult.getResponse().getContentAsString(), AuthorView.class);
    assertNotNull(authorView.id(), "Author id must not be null!");
    assertEquals(goodRequest.fullName(), authorView.fullName(), "Author name update isn't applied!");
  }

  @Test
  public void testCreateFail() throws Exception {
    EditAuthorRequest badRequest = new EditAuthorRequest();

    this.mockMvc
      .perform(post("/api/author")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, badRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testEditSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");

    EditAuthorRequest updateRequest = EditAuthorRequest.builder().fullName("Test Author B").about("Cool author").build();

    MvcResult updateResult = this.mockMvc
      .perform(put(String.format("/api/author/%s", authorView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isOk())
      .andReturn();
    AuthorView newAuthorView = fromJson(objectMapper, updateResult.getResponse().getContentAsString(),
      AuthorView.class);

    assertEquals(updateRequest.fullName(), newAuthorView.fullName(), "Author name update isn't applied!");
    assertEquals(updateRequest.about(), newAuthorView.about(), "Author name update isn't applied!");
  }

  @Test
  public void testEditFailBadRequest() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");

    EditAuthorRequest updateRequest = new EditAuthorRequest();

    this.mockMvc
      .perform(put(String.format("/api/author/%s", authorView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testEditFailNotFound() throws Exception {
    EditAuthorRequest updateRequest = EditAuthorRequest.builder().fullName("Test Author B").build();

    this.mockMvc
      .perform(put(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  public void testDeleteSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");

    this.mockMvc
      .perform(delete(String.format("/api/author/%s", authorView.id())))
      .andExpect(status().isOk());

    this.mockMvc
      .perform(get(String.format("/api/author/%s", authorView.id())))
      .andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteFailNotFound() throws Exception {
    this.mockMvc
      .perform(delete(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  @WithAnonymousUser
  public void testGetSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");

    MvcResult getResult = this.mockMvc
      .perform(get(String.format("/api/author/%s", authorView.id())))
      .andExpect(status().isOk())
      .andReturn();

    AuthorView newAuthorView = fromJson(objectMapper, getResult.getResponse().getContentAsString(), AuthorView.class);

    assertEquals(authorView.id(), newAuthorView.id(), "Author ids must be equal!");
  }

  @Test
  @WithAnonymousUser
  public void testGetNotFound() throws Exception {
    this.mockMvc
      .perform(get(String.format("/api/author/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity Author with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  @WithAnonymousUser
  public void testGetAuthorBooksSuccess() throws Exception {
    AuthorView authorView = authorTestDataFactory.createAuthor("Test Author A");
    BookView bookView1 = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book A");
    BookView bookView2 = bookTestDataFactory.createBook(List.of(authorView.id()), "Test Book B");

    MvcResult getBooksResult = this.mockMvc
      .perform(get(String.format("/api/author/%s/book", authorView.id())))
      .andExpect(status().isOk())
      .andReturn();

    ListResponse<BookView> bookViewList = fromJson(objectMapper,
      getBooksResult.getResponse().getContentAsString(),
      new TypeReference<>() {
      });

    assertEquals(2, bookViewList.items().size(), "Author must have 2 books");
    assertEquals(bookView1.title(), bookViewList.items().get(0).title(), "Book title mismatch!");
    assertEquals(bookView2.title(), bookViewList.items().get(1).title(), "Book title mismatch!");
  }

}
