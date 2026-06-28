package com.example.starterkit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
