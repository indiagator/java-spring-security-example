package io.example.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.example.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static java.lang.String.format;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

  private final UserRepo userRepo;

  @Value("${jwt.public.key}")
  private RSAPublicKey rsaPublicKey;

  @Value("${jwt.private.key}")
  private RSAPrivateKey rsaPrivateKey;

  @Value("${springdoc.api-docs.path}")
  private String restApiDocPath;

  @Value("${springdoc.swagger-ui.path}")
  private String swaggerPath;

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> userRepo
      .findByUsername(username)
      .orElseThrow(
        () ->
          new UsernameNotFoundException(format("User: %s, not found", username)));
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Set permissions on endpoints
    http.authorizeHttpRequests(registry -> registry
      // Swagger endpoints must be publicly accessible
      .requestMatchers("/")
      .permitAll()
      .requestMatchers("/index.html")
      .permitAll()
      .requestMatchers("/favicon.ico")
      .permitAll()
      .requestMatchers(format("%s/**", restApiDocPath))
      .permitAll()
      .requestMatchers(format("%s/**", swaggerPath))
      .permitAll()
      // Our public endpoints
      .requestMatchers("/api/public/**")
      .permitAll()
      .requestMatchers(HttpMethod.GET, "/api/author/**")
      .permitAll()
      .requestMatchers(HttpMethod.POST, "/api/author/search")
      .permitAll()
      .requestMatchers(HttpMethod.GET, "/api/book/**")
      .permitAll()
      .requestMatchers(HttpMethod.POST, "/api/book/search")
      .permitAll()
      // Our private endpoints
      .anyRequest()
      .authenticated());

    // Set up oauth2 resource server
    http.httpBasic(Customizer.withDefaults());
    http.oauth2ResourceServer(configurer ->
      configurer.jwt(jwtConfigurer ->
        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    // Disable CSRF
    http.csrf(AbstractHttpConfigurer::disable);

    // Set session management to stateless
    http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Set unauthorized requests exception handler
    http.exceptionHandling(
      (exceptions) ->
        exceptions
          .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
          .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

    return http.build();
  }

  // Used by JwtAuthenticationProvider to generate JWT tokens
  @Bean
  public JwtEncoder jwtEncoder() {
    var jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
    var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }

  // Used by JwtAuthenticationProvider to decode and validate JWT tokens
  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
  }

  // Extract authorities from the roles claim
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    var jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  // Set password encoding schema
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Enable CORS for the REST API
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  // Expose authentication manager bean
  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
