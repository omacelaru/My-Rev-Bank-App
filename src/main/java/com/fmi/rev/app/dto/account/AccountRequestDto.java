package com.fmi.rev.app.dto.account;

import com.fmi.rev.app.model.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountRequestDto {
    @NotNull
    private Currency currency;
}
