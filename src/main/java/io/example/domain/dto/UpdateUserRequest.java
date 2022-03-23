package io.example.domain.dto;

import java.util.Set;

import javax.validation.constraints.NotBlank;

import lombok.Builder;

public record UpdateUserRequest(
	@NotBlank
	String fullName,
	Set<String> authorities
) {

	@Builder
	public UpdateUserRequest {
	}

	public UpdateUserRequest() {
		this(null, null);
	}
}
