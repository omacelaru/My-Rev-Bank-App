package com.fmi.rev.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserCreationRequestDto {
    @NotBlank
    private String username;
    @NotEmpty
    private String password;
    @NotNull
    @Pattern(regexp = "ROLE_[A-Z]+(,ROLE_[A-Z]+)*")
    private String roles;
}
