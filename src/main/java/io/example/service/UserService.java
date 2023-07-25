package io.example.service;

import io.example.domain.dto.*;
import io.example.domain.mapper.UserEditMapper;
import io.example.domain.mapper.UserViewMapper;
import io.example.domain.model.User;
import io.example.repository.UserRepo;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

  private final UserRepo userRepo;
  private final UserEditMapper userEditMapper;
  private final UserViewMapper userViewMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserView create(CreateUserRequest request) {
    if (userRepo.findByUsername(request.username()).isPresent()) {
      throw new ValidationException("Username exists!");
    }
    if (!request.password().equals(request.rePassword())) {
      throw new ValidationException("Passwords don't match!");
    }

    var user = userEditMapper.create(request);
    user.setPassword(passwordEncoder.encode(request.password()));

    user = userRepo.save(user);

    return userViewMapper.toUserView(user);
  }

  @Transactional
  public UserView update(ObjectId id, UpdateUserRequest request) {
    var user = userRepo.getById(id);
    userEditMapper.update(request, user);

    user = userRepo.save(user);

    return userViewMapper.toUserView(user);
  }

  @Transactional
  public void upsert(CreateUserRequest request) {
    var optionalUser = userRepo.findByUsername(request.username());

    if (optionalUser.isEmpty()) {
      create(request);
    } else {
      UpdateUserRequest updateUserRequest =
          new UpdateUserRequest(request.fullName(), request.authorities());
      update(optionalUser.get().getId(), updateUserRequest);
    }
  }

  @Transactional
  public UserView delete(ObjectId id) {
    var user = userRepo.getById(id);

    user.setUsername(
        user.getUsername().replace("@", String.format("_%s@", user.getId().toString())));
    user.setEnabled(false);
    user = userRepo.save(user);

    return userViewMapper.toUserView(user);
  }

  public UserView getUser(ObjectId id) {
    return userViewMapper.toUserView(userRepo.getById(id));
  }

  public List<UserView> searchUsers(Page page, SearchUsersQuery query) {
    List<User> users = userRepo.searchUsers(page, query);
    return userViewMapper.toUserView(users);
  }
}
