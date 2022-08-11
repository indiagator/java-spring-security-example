package io.example.configuration;

import static java.util.Optional.ofNullable;

import io.example.domain.model.User;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

  @Bean
  public AuditorAware<ObjectId> auditorProvider() {
    return () -> {
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      User user = null;
      if (authentication != null && authentication.getPrincipal() instanceof User) {
        user = (User) authentication.getPrincipal();
      }
      return ofNullable(user).map(User::getId);
    };
  }
}
