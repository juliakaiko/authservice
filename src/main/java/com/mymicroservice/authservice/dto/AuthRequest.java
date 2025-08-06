package com.mymicroservice.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request")
public class AuthRequest {

    @Email(regexp="\\w+@\\w+\\.\\w+", message="Please provide a valid email address")
    @NotBlank(message = "Email address may not be blank")
    private String email;

    @NotBlank (message = "Password may not be blank")
    @Size(min=5, max=255, message = "Password size must be between 5 and 255")
    private String password;
}
