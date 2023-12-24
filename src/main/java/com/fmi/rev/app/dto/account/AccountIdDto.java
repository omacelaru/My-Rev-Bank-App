package com.fmi.rev.app.dto.account;

import com.fmi.rev.app.model.Account;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountIdDto {
    @NotNull
    private Long id;

    public static AccountIdDto fromAccount(Account account) {
        return new AccountIdDto(account.getId());
    }
}
