package com.fmi.rev.app.dto.transaction;

import com.fmi.rev.app.dto.account.AccountIdDto;
import com.fmi.rev.app.model.Transaction;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionResponseDto {
    private Long id;
    private Transaction.Type type;
    private Double amount;
    private Transaction.State state;
    private LocalDateTime time;
    private Double exchangeRate;
    protected AccountIdDto from;
    protected AccountIdDto to;

    public static TransactionResponseDto fromTransaction(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getState(),
                transaction.getTime(),
                transaction.getExchangeRate(),
                AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo())
        );
    }
}
