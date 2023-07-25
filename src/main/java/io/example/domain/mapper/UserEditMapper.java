package io.example.domain.mapper;

import io.example.domain.dto.CreateUserRequest;
import io.example.domain.dto.UpdateUserRequest;
import io.example.domain.model.Role;
import io.example.domain.model.User;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = ObjectIdMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserEditMapper {

  @Mapping(source = "authorities", target = "authorities", qualifiedByName = "stringToRole")
  public abstract User create(CreateUserRequest request);

  @BeanMapping(nullValueCheckStrategy = ALWAYS, nullValuePropertyMappingStrategy = IGNORE)
  @Mapping(source = "authorities", target = "authorities", qualifiedByName = "stringToRole")
  public abstract void update(UpdateUserRequest request, @MappingTarget User user);

  @Named("stringToRole")
  protected Set<Role> stringToRole(Set<String> authorities) {
    if (authorities != null) {
      return authorities.stream().map(Role::new).collect(toSet());
    }
    return new HashSet<>();
  }

}
