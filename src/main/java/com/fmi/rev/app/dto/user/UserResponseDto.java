package com.fmi.rev.app.dto.user;

import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private String roles;
    private Set<AccountResponseDto> accounts;

    public static UserResponseDto fromUser(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRoles(),
                user.getAccounts().stream().map(AccountResponseDto::fromAccount).collect(Collectors.toSet())
        );
    }
}
