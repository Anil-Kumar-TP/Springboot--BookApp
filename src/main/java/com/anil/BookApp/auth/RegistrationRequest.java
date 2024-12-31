package com.anil.BookApp.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotEmpty(message = "FirstName is required")
    @NotBlank(message = "FirstName is required")
    private String firstname;

    @NotEmpty(message = "LastName is required")
    @NotBlank(message = "LastName is required")
    private String lastname;

    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Size(min = 8,message = "Password must be greater than 8 characters")
    private String password;
}
