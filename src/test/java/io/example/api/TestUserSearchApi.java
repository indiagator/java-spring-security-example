package io.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.UserTestDataFactory;
import io.example.domain.dto.ListResponse;
import io.example.domain.dto.SearchRequest;
import io.example.domain.dto.SearchUsersQuery;
import io.example.domain.dto.UserView;
import io.example.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static io.example.util.JsonHelper.fromJson;
import static io.example.util.JsonHelper.toJson;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = Role.USER_ADMIN)
public class TestUserSearchApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final UserTestDataFactory userTestDataFactory;

  @Autowired
  public TestUserSearchApi(MockMvc mockMvc, ObjectMapper objectMapper, UserTestDataFactory userTestDataFactory) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.userTestDataFactory = userTestDataFactory;
  }

  @Test
  public void testSearch() throws Exception {
    UserView user1 = userTestDataFactory.createUser(String.format("william.baker.%d@gmail.com", currentTimeMillis()),
      "William Baker");
    UserView user2 = userTestDataFactory.createUser(String.format("james.adams.%d@gmail.com", currentTimeMillis()),
      "James Adams");
    UserView user3 = userTestDataFactory.createUser(String.format("evelin.clarke.%d@nix.io", currentTimeMillis()),
      "Evelyn Clarke");
    UserView user4 = userTestDataFactory.createUser(String.format("ella.davidson.%d@nix.io", currentTimeMillis()),
      "Ella Davidson");
    UserView user5 = userTestDataFactory.createUser(String.format("evelin.bradley.%d@outlook.com", currentTimeMillis()),
      "Evelyn Bradley");

    testIdFilter(user1.id());
    testUsernameFilter();
    testFullNameFilter();

    userTestDataFactory.deleteUser(user1.id());
    userTestDataFactory.deleteUser(user2.id());
    userTestDataFactory.deleteUser(user3.id());
    userTestDataFactory.deleteUser(user4.id());
    userTestDataFactory.deleteUser(user5.id());
  }

  private void testIdFilter(String id) throws Exception {
    SearchUsersQuery query;
    ListResponse<UserView> userViewList;

    // Search query with book id equal
    query = SearchUsersQuery.builder().id(id).build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(1, userViewList.items().size(), "Invalid search result!");
  }

  private void testUsernameFilter() throws Exception {
    SearchUsersQuery query;
    ListResponse<UserView> userViewList;

    // Search query username starts with
    query = SearchUsersQuery.builder().username("evelin").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(2, userViewList.items().size(), "Invalid search result!");

    // Search query username contains
    query = SearchUsersQuery.builder().username("gmail").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(2, userViewList.items().size(), "Invalid search result!");

    // Search query username case insensitive
    query = SearchUsersQuery.builder().username("William").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(1, userViewList.items().size(), "Invalid search result!");
  }

  private void testFullNameFilter() throws Exception {
    SearchUsersQuery query;
    ListResponse<UserView> userViewList;

    // Search query full name starts with
    query = SearchUsersQuery.builder().username("William").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(1, userViewList.items().size(), "Invalid search result!");

    // Search query full name contains
    query = SearchUsersQuery.builder().username("David").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(1, userViewList.items().size(), "Invalid search result!");

    // Search query full name case insensitive
    query = SearchUsersQuery.builder().username("CLARKE").build();
    userViewList = execute("/api/admin/user/search", query);
    assertEquals(1, userViewList.items().size(), "Invalid search result!");
  }

  private ListResponse<UserView> execute(String url, SearchUsersQuery query) throws Exception {
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
