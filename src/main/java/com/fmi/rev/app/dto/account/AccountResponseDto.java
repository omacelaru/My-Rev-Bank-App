package com.fmi.rev.app.dto.account;

import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountResponseDto {
    private Long id;
    private Currency currency;
    private Double balance;
    private List<TransactionResponseDto> transactions;

    public static AccountResponseDto fromAccount(Account account) {
        Stream<TransactionResponseDto> transactionsSent = account.getTransactionsSent()
                .stream().map(TransactionResponseDto::fromTransaction);
        Stream<TransactionResponseDto> transactionsReceived = account.getTransactionsReceived()
                .stream().map(TransactionResponseDto::fromTransaction);
        List<TransactionResponseDto> transactions = Stream.concat(transactionsSent, transactionsReceived)
                .distinct().sorted(Comparator.comparing(TransactionResponseDto::getTime)).toList();

        return new AccountResponseDto(
                account.getId(),
                account.getCurrency(),
                account.getBalance(),
                transactions
        );
    }
}
