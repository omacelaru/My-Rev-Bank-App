package com.fmi.rev.app.service;

import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.dto.user.UserCreationRequestDto;
import com.fmi.rev.app.dto.user.UserCredentialsDto;
import com.fmi.rev.app.dto.user.UserResponseDto;
import com.fmi.rev.app.exception.AccountNotFoundException;
import com.fmi.rev.app.exception.TransactionNotFoundException;
import com.fmi.rev.app.exception.UnauthorizedException;
import com.fmi.rev.app.exception.UserNotFoundException;
import com.fmi.rev.app.model.Currency;
import com.fmi.rev.app.model.Transaction;
import com.fmi.rev.app.repository.UserRepository;
import com.fmi.rev.app.security.JwtProvider;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final long TTL = 60;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Optional<User> getUser(String username) {
        return userRepository.findUserByUsername(username);
    }

    public UserResponseDto createUser(UserCreationRequestDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User user = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        userRepository.save(user);
        return UserResponseDto.fromUser(user);
    }

    public String authenticate(UserCredentialsDto userDto) {
        User user = getUser(userDto.getUsername()).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }
        Set<String> roles = Arrays.stream(user.getRoles().split(",")).collect(Collectors.toSet());
        return jwtProvider.generateToken(userDto.getUsername(), TTL, roles);
    }

    public UserResponseDto getUserDto(String username) {
        User user = getUser(username).orElseThrow(UnauthorizedException::new);
        return UserResponseDto.fromUser(user);
    }

    public UserResponseDto getUserDto(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return UserResponseDto.fromUser(user);
    }

    public List<UserResponseDto> getUsers() {
        return userRepository.findAll().stream().map(UserResponseDto::fromUser).toList();
    }

    public List<AccountResponseDto> getAccountsOfUser(String username, Currency currency, Double minBalance, Double maxBalance) {
        User user = getUser(username).orElseThrow(UnauthorizedException::new);
        Stream<Account> accounts = user.getAccounts().stream();
        List<Predicate<Account>> filters = new ArrayList<>();
        if (currency != null) {
            filters.add(acc -> acc.getCurrency() == currency);
        }
        if (minBalance != null) {
            filters.add(acc -> acc.getBalance() >= minBalance);
        }
        if (maxBalance != null) {
            filters.add(acc -> acc.getBalance() <= maxBalance);
        }
        return accounts.filter(filters.stream().reduce(acc -> true, Predicate::and)).map(AccountResponseDto::fromAccount).toList();
    }

    public AccountResponseDto getAccountOfUser(String username, Long accountId) {
        User user = getUser(username).orElseThrow(UnauthorizedException::new);
        Account account = user.getAccountById(accountId).orElseThrow(AccountNotFoundException::new);
        return AccountResponseDto.fromAccount(account);
    }

    public List<TransactionResponseDto> getTransactionsOfUserAccount(
            String username, Long accountId, Transaction.Type type, Double minAmount, Double maxAmount,
            Transaction.State state, LocalDateTime fromTime, LocalDateTime untilTime, Long fromAccount, Long toAccount) {
        User user = getUser(username).orElseThrow(UnauthorizedException::new);
        Account account = user.getAccountById(accountId).orElseThrow(AccountNotFoundException::new);
        Stream<TransactionResponseDto> transactions = AccountResponseDto.fromAccount(account).getTransactions().stream();
        List<Predicate<TransactionResponseDto>> filters = new ArrayList<>();
        if (type != null) {
            filters.add(t -> t.getType() == type);
        }
        if (minAmount != null) {
            filters.add(t -> t.getAmount() >= minAmount);
        }
        if (maxAmount != null) {
            filters.add(t -> t.getAmount() <= maxAmount);
        }
        if (state != null) {
            filters.add(t -> t.getState() == state);
        }
        if (fromTime != null) {
            filters.add(t -> !t.getTime().isBefore(fromTime));
        }
        if (untilTime != null) {
            filters.add(t -> !t.getTime().isAfter(untilTime));
        }
        if (fromAccount != null) {
            filters.add(t -> t.getFrom().getId().equals(fromAccount));
        }
        if (toAccount != null) {
            filters.add(t -> t.getTo().getId().equals(toAccount));
        }
        return transactions.filter(filters.stream().reduce(t -> true, Predicate::and)).toList();
    }

    public TransactionResponseDto getTransactionOfUserAccount(String username, Long accountId, Long transactionId) {
        User user = getUser(username).orElseThrow(UnauthorizedException::new);
        Account account = user.getAccountById(accountId).orElseThrow(AccountNotFoundException::new);
        Transaction transaction = account.getTransactions().stream().filter(t -> t.getId().equals(transactionId))
                .findFirst().orElseThrow(TransactionNotFoundException::new);
        return TransactionResponseDto.fromTransaction(transaction);
    }
}
