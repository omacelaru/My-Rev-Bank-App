package com.fmi.rev.app.dto.transaction;

import com.fmi.rev.app.dto.account.AccountIdDto;
import com.fmi.rev.app.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionRequestDto {
    @NotNull
    private Transaction.Type type;
    @NotNull
    @Positive
    private Double amount;
    @NotNull
    @Valid
    private AccountIdDto from;
    @NotNull
    @Valid
    private AccountIdDto to;
}
