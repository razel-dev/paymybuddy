package com.alcaniz.paymybuddy.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserConnectionDTO(
		@NotNull
		@Positive
		Integer ownerUserId,

		@NotNull
		@Positive
		Integer relatedUserId
) {
	// Reflète le CHECK (owner_user_id <> related_user_id) du schéma
	@AssertTrue(message = "ownerUserId et relatedUserId doivent être différents")
	public boolean isNotSelfConnection() {
		return ownerUserId != null
				&& relatedUserId != null
				&& !ownerUserId.equals(relatedUserId);
	}
}