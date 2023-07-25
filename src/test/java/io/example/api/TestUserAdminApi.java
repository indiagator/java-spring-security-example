package io.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.api.data.UserTestDataFactory;
import io.example.domain.dto.CreateUserRequest;
import io.example.domain.dto.UpdateUserRequest;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = Role.USER_ADMIN)
public class TestUserAdminApi {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final UserTestDataFactory userTestDataFactory;

  @Autowired
  public TestUserAdminApi(MockMvc mockMvc, ObjectMapper objectMapper, UserTestDataFactory userTestDataFactory) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.userTestDataFactory = userTestDataFactory;
  }

  @Test
  public void testCreateSuccess() throws Exception {
    CreateUserRequest goodRequest = new CreateUserRequest(
      String.format("test.user.%d@nix.com", currentTimeMillis()),
      "Test User A",
      "Test12345_"
    );

    MvcResult createResult = this.mockMvc
      .perform(post("/api/admin/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, goodRequest)))
      .andExpect(status().isOk())
      .andReturn();

    UserView userView = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserView.class);
    assertNotNull(userView.id(), "User id must not be null!");
    assertEquals(goodRequest.fullName(), userView.fullName(), "User fullname  update isn't applied!");
  }

  @Test
  public void testCreateFail() throws Exception {
    CreateUserRequest badRequest = new CreateUserRequest(
      "invalid.username", "", ""
    );

    this.mockMvc
      .perform(post("/api/admin/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, badRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testCreateUsernameExists() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    CreateUserRequest badRequest = new CreateUserRequest(
      userView.username(),
      "Test User A",
      "Test12345_"
    );

    this.mockMvc
      .perform(post("/api/admin/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, badRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Username exists")));
  }

  @Test
  public void testCreatePasswordsMismatch() throws Exception {
    CreateUserRequest badRequest = new CreateUserRequest(
      String.format("test.user.%d@nix.com", currentTimeMillis()),
      "Test User A",
      "Test12345_",
      "Test12345"
    );

    this.mockMvc
      .perform(post("/api/admin/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, badRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Passwords don't match")));
  }

  @Test
  public void testEditSuccess() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    UpdateUserRequest updateRequest = new UpdateUserRequest(
      "Test User B", null
    );

    MvcResult updateResult = this.mockMvc
      .perform(put(String.format("/api/admin/user/%s", userView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isOk())
      .andReturn();
    UserView newUserView = fromJson(objectMapper, updateResult.getResponse().getContentAsString(), UserView.class);

    assertEquals(updateRequest.fullName(), newUserView.fullName(), "User fullname update isn't applied!");
  }

  @Test
  public void testEditFailBadRequest() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    UpdateUserRequest updateRequest = new UpdateUserRequest();

    this.mockMvc
      .perform(put(String.format("/api/admin/user/%s", userView.id()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString("Method argument validation failed")));
  }

  @Test
  public void testEditFailNotFound() throws Exception {
    UpdateUserRequest updateRequest = new UpdateUserRequest(
      "Test User B", null
    );

    this.mockMvc
      .perform(put(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, updateRequest)))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  public void testDeleteSuccess() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    this.mockMvc
      .perform(delete(String.format("/api/admin/user/%s", userView.id())))
      .andExpect(status().isOk());

    this.mockMvc
      .perform(get(String.format("/api/admin/user/%s", userView.id())))
      .andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteFailNotFound() throws Exception {
    this.mockMvc
      .perform(delete(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
  }

  @Test
  public void testDeleteAndCreateAgain() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    this.mockMvc
      .perform(delete(String.format("/api/admin/user/%s", userView.id())))
      .andExpect(status().isOk());

    this.mockMvc
      .perform(get(String.format("/api/admin/user/%s", userView.id())))
      .andExpect(status().isNotFound());

    CreateUserRequest goodRequest = new CreateUserRequest(
      userView.username(),
      "Test User A",
      "Test12345_"
    );

    MvcResult createResult = this.mockMvc
      .perform(post("/api/admin/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(objectMapper, goodRequest)))
      .andExpect(status().isOk())
      .andReturn();

    UserView newUserView = fromJson(objectMapper, createResult.getResponse().getContentAsString(), UserView.class);
    assertNotEquals(userView.id(), newUserView.id(), "User ids must not match!");
    assertEquals(userView.username(), newUserView.username(), "User names must match!");
  }

  @Test
  public void testGetSuccess() throws Exception {
    UserView userView = userTestDataFactory.createUser(String.format("test.user.%d@nix.io", currentTimeMillis()),
      "Test User A");

    MvcResult getResult = this.mockMvc
      .perform(get(String.format("/api/admin/user/%s", userView.id())))
      .andExpect(status().isOk())
      .andReturn();

    UserView newUserView = fromJson(objectMapper, getResult.getResponse().getContentAsString(), UserView.class);

    assertEquals(userView.id(), newUserView.id(), "User ids must be equal!");
  }

  @Test
  public void testGetNotFound() throws Exception {
    this.mockMvc
      .perform(get(String.format("/api/admin/user/%s", "5f07c259ffb98843e36a2aa9")))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Entity User with id 5f07c259ffb98843e36a2aa9 not found")));
  }

}
