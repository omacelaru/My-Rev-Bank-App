package com.fmi.rev.app.service;

import com.fmi.rev.app.dto.transaction.ActionDto;
import com.fmi.rev.app.dto.transaction.TransactionRequestDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.exception.*;
import com.fmi.rev.app.repository.TransactionRepository;
import com.fmi.rev.app.exception.*;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.Transaction;
import com.fmi.rev.app.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountService accountService;
    private final ExchangeRatesService exchangeRatesService;

    @Transactional
    public TransactionResponseDto userMakesTransaction(String username, TransactionRequestDto transactionDto) {
        User user = userService.getUser(username).orElseThrow(UnauthorizedException::new);
        Transaction.Type type = transactionDto.getType();
        Double amount = transactionDto.getAmount();
        Long fromId = transactionDto.getFrom().getId();
        Long toId = transactionDto.getTo().getId();
        Account fromAccount = user.getAccountById(fromId).orElseThrow(InvalidAccountException::new);
        Account toAccount;
        double exchangeRate = 1.0;
        if (type == Transaction.Type.Deposit || type == Transaction.Type.Withdrawal) {
            if (!fromId.equals(toId)) {
                throw new InvalidAccountException();
            }
            toAccount = fromAccount;
        } else {
            if (fromId.equals(toId)) {
                throw new InvalidAccountException();
            }
            toAccount = accountService.getAccount(toId).orElseThrow(InvalidAccountException::new);
        }
        switch (type) {
            case Deposit -> accountService.increaseAccountBalance(fromAccount, amount);
            case Withdrawal -> {
                if (!accountService.decreaseAccountBalance(fromAccount, amount)) {
                    throw new InvalidAmountException();
                }
            }
            case Payment -> {
                exchangeRate = exchangeRatesService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
                if (!accountService.decreaseAccountBalance(fromAccount, amount)) {
                    throw new InvalidAmountException();
                }
                accountService.increaseAccountBalance(toAccount, amount * exchangeRate);
            }
            case Request -> exchangeRate = 0.0;
        }
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), exchangeRate, fromAccount, toAccount);
        transactionRepository.save(transaction);
        return TransactionResponseDto.fromTransaction(transaction);
    }

    @Transactional
    public TransactionResponseDto userRespondsToRequest(String username, Long transactionId, ActionDto actionDto) {
        User user = userService.getUser(username).orElseThrow(UnauthorizedException::new);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(ForbiddenException::new);
        Account toAccount = transaction.getTo();
        if (transaction.getType() != Transaction.Type.Request
                || transaction.getState() != Transaction.State.Pending
                || !user.getId().equals(toAccount.getUser().getId())) {
            throw new ForbiddenException();
        }
        if (actionDto.getAction() == ActionDto.Action.Deny) {
            transaction.setState(Transaction.State.Denied);
        } else {
            Account fromAccount = transaction.getFrom();
            Double amount = transaction.getAmount();
            Double exchangeRate = exchangeRatesService.getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
            if (!accountService.decreaseAccountBalance(transaction.getTo(), amount * exchangeRate)) {
                throw new InvalidAmountException();
            }
            accountService.increaseAccountBalance(fromAccount, amount);
            transaction.setState(Transaction.State.Accepted);
            transaction.setExchangeRate(exchangeRate);
        }
        transaction.setTime(LocalDateTime.now());
        transactionRepository.save(transaction);
        return TransactionResponseDto.fromTransaction(transaction);
    }

    public TransactionResponseDto getTransactionDto(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(TransactionNotFoundException::new);
        return TransactionResponseDto.fromTransaction(transaction);
    }

    public List<TransactionResponseDto> getTransactions() {
        return transactionRepository.findAll().stream().map(TransactionResponseDto::fromTransaction).toList();
    }
}
