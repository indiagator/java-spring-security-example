package io.example;

import io.example.domain.dto.CreateUserRequest;
import io.example.domain.model.Role;
import io.example.service.UserService;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationListener<ApplicationReadyEvent> {

  private final List<String> usernames =
      List.of("ada.lovelace@nix.io", "alan.turing@nix.io", "dennis.ritchie@nix.io");
  private final List<String> fullNames = List.of("Ada Lovelace", "Alan Turing", "Dennis Ritchie");
  private final List<String> roles = List.of(Role.USER_ADMIN, Role.AUTHOR_ADMIN, Role.BOOK_ADMIN);
  private final String password = "Test12345_";

  private final UserService userService;

  public DatabaseInitializer(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
    for (var i = 0; i < usernames.size(); ++i) {
      var request =
          new CreateUserRequest(
              usernames.get(i), fullNames.get(i), password, password, Set.of(roles.get(i)));

      userService.upsert(request);
    }
  }
}
