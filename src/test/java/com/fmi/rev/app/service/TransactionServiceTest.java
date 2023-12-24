package com.fmi.rev.app.service;

import com.fmi.rev.app.dto.account.AccountIdDto;
import com.fmi.rev.app.dto.transaction.ActionDto;
import com.fmi.rev.app.dto.transaction.TransactionRequestDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.dto.user.UserCreationRequestDto;
import com.fmi.rev.app.exception.ForbiddenException;
import com.fmi.rev.app.exception.InvalidAccountException;
import com.fmi.rev.app.exception.InvalidAmountException;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.Currency;
import com.fmi.rev.app.model.Transaction;
import com.fmi.rev.app.model.User;
import com.fmi.rev.app.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final UserService userService = mock(UserService.class);
    private final AccountService accountService = mock(AccountService.class);
    private final ExchangeRatesService exchangeRatesService = mock(ExchangeRatesService.class);
    private TransactionService transactionService;

    @BeforeEach
    public void setUp() {
        transactionService = new TransactionService(transactionRepository, userService, accountService, exchangeRatesService);
    }

    @Test
    public void shouldMakesTransactionDepositTransactionSuccessful() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 150.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Deposit;
        User createdUser = mock(User.class);
        Account account = new Account(Currency.USD);
        account.setId(accountId);
        account.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(account));

        TransactionResponseDto response = transactionService.userMakesTransaction(createdUser.getUsername(), requestDto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void shouldThrowsInvalidAccountExceptionWhenMakesDepositDifferentAccounts() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 150.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Deposit;
        User createdUser = mock(User.class);
        Account account = new Account(Currency.USD);
        Account account2 = new Account(Currency.EUR);
        account.setId(accountId);
        account.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, account, account2);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountException.class, () -> transactionService.userMakesTransaction(createdUser.getUsername(), requestDto));
    }

    @Test
    public void shouldMakesTransactionWithdrawalTransactionSuccessful() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 50.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Withdrawal;
        User createdUser = mock(User.class);
        Account account = new Account(Currency.USD);
        account.setId(accountId);
        account.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(accountService.decreaseAccountBalance(any(), any())).thenReturn(true);

        TransactionResponseDto response = transactionService.userMakesTransaction(createdUser.getUsername(), requestDto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void shouldThrowsInvalidAmountExceptionWhenMakesWithdrawalWithoutMoney() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 150.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Withdrawal;
        User createdUser = mock(User.class);
        Account account = new Account(Currency.USD);
        account.setId(accountId);
        account.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(accountService.decreaseAccountBalance(any(), any())).thenReturn(false);

        assertThrows(InvalidAmountException.class, () -> transactionService.userMakesTransaction(createdUser.getUsername(), requestDto));
    }

    @Test
    public void shouldMakesTransactionPaymentTransactionSuccessful() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 50.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Payment;
        User createdUser = mock(User.class);
        Account accountFrom = new Account(Currency.USD);
        Account accountTo = new Account(Currency.EUR);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(accountFrom));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(accountFrom));
        when(accountService.getAccount(any())).thenReturn(Optional.of(accountTo));
        when(accountService.decreaseAccountBalance(any(), any())).thenReturn(true);

        TransactionResponseDto response = transactionService.userMakesTransaction(createdUser.getUsername(), requestDto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void shouldThrowsInvalidAccountExceptionWhenMakesPaymentSameAccounts() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 50.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Payment;
        User createdUser = mock(User.class);
        Account account = new Account(Currency.USD);
        account.setId(accountId);
        account.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(accountService.decreaseAccountBalance(any(), any())).thenReturn(true);

        assertThrows(InvalidAccountException.class, () -> transactionService.userMakesTransaction(createdUser.getUsername(), requestDto));
    }

    @Test
    public void shouldThrowsInvalidAmountExceptionWhenMakesPaymentWithoutMoney() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 50.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Payment;
        User createdUser = mock(User.class);
        Account accountFrom = new Account(Currency.USD);
        Account accountTo = new Account(Currency.EUR);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(accountFrom));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(accountFrom));
        when(accountService.getAccount(any())).thenReturn(Optional.of(accountTo));
        when(accountService.decreaseAccountBalance(any(), any())).thenReturn(false);

        assertThrows(InvalidAmountException.class, () -> transactionService.userMakesTransaction(createdUser.getUsername(), requestDto));
    }

    @Test
    public void shouldMakesTransactionRequestTransactionSuccessful() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double amount = 50.0;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Request;
        User createdUser = mock(User.class);
        Account accountFrom = new Account(Currency.USD);
        Account accountTo = new Account(Currency.EUR);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), 1.0, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(accountFrom));
        createdUser.setAccounts(accounts);
        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(), transaction.getAmount(), AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));


        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        when(createdUser.getAccountById(anyLong())).thenReturn(Optional.of(accountFrom));
        when(accountService.getAccount(any())).thenReturn(Optional.of(accountTo));

        TransactionResponseDto response = transactionService.userMakesTransaction(createdUser.getUsername(), requestDto);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void shouldGetTransactionDto() {
        Long transactionId = 1L;
        Account account = new Account();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setFrom(account);
        transaction.setTo(account);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        transactionService.getTransactionDto(transactionId);

        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    public void shouldGetTransactions() {
        transactionService.getTransactions();
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    public void userShouldRespondsToRequestWithAccept() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Request;
        Double amount = 100D;
        Double exchangeRate = 1.2;

        UserCreationRequestDto userCreationDto1 = new UserCreationRequestDto("user_test1", "user_test1", "ROLE_USER");
        User createdUserFrom = new User(userCreationDto1.getUsername(), "user_test1", userCreationDto1.getRoles());
        createdUserFrom.setId(1L);

        UserCreationRequestDto userCreationDto2 = new UserCreationRequestDto("user_test2", "user_test2", "ROLE_USER");
        User createdUserTo = new User(userCreationDto2.getUsername(), "user_test2", userCreationDto2.getRoles());
        createdUserTo.setId(2L);

        Account accountFrom = new Account(Currency.USD);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountFrom.setUser(createdUserFrom);

        Account accountTo = new Account(Currency.EUR);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);
        accountTo.setUser(createdUserTo);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), exchangeRate, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        accountTo.setTransactionsReceived(transactions);

        Set<Account> accountsFrom = new HashSet<>(Collections.singleton(accountFrom));
        createdUserFrom.setAccounts(accountsFrom);
        Set<Account> accountsTo = new HashSet<>(Collections.singleton(accountTo));
        createdUserTo.setAccounts(accountsTo);

        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(),
                transaction.getAmount(),
                AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));

        ActionDto actionDto = new ActionDto(ActionDto.Action.Accept);

        when(userService.getUser(createdUserFrom.getUsername())).thenReturn(Optional.of(createdUserTo));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(exchangeRatesService.getExchangeRate(accountFrom.getCurrency(), accountTo.getCurrency()))
                .thenReturn(exchangeRate);
        when(accountService.decreaseAccountBalance(accountTo, amount * exchangeRate)).thenReturn(true);


        TransactionResponseDto responseDto = transactionService.userRespondsToRequest(createdUserFrom.getUsername(), transactionId, actionDto);

        assertEquals(Transaction.State.Accepted, transaction.getState());
        assertEquals(exchangeRate, transaction.getExchangeRate());
        assertNotNull(transaction.getTime());

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void shouldThrowsForbiddenExceptionWhenTransactionDoesNotMatch() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Request;
        Double amount = 100D;
        Double exchangeRate = 1.2;

        UserCreationRequestDto userCreationDto1 = new UserCreationRequestDto("user_test1", "user_test1", "ROLE_USER");
        User createdUserFrom = new User(userCreationDto1.getUsername(), "user_test1", userCreationDto1.getRoles());
        createdUserFrom.setId(1L);

        UserCreationRequestDto userCreationDto2 = new UserCreationRequestDto("user_test2", "user_test2", "ROLE_USER");
        User createdUserTo = new User(userCreationDto2.getUsername(), "user_test2", userCreationDto2.getRoles());
        createdUserTo.setId(2L);

        Account accountFrom = new Account(Currency.USD);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountFrom.setUser(createdUserFrom);

        Account accountTo = new Account(Currency.EUR);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);
        accountTo.setUser(createdUserTo);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), exchangeRate, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        accountTo.setTransactionsReceived(transactions);

        Set<Account> accountsFrom = new HashSet<>(Collections.singleton(accountFrom));
        createdUserFrom.setAccounts(accountsFrom);
        Set<Account> accountsTo = new HashSet<>(Collections.singleton(accountTo));
        createdUserTo.setAccounts(accountsTo);

        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(),
                transaction.getAmount(),
                AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));

        ActionDto actionDto = new ActionDto(ActionDto.Action.Accept);

        when(userService.getUser(createdUserFrom.getUsername())).thenReturn(Optional.of(createdUserFrom));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(exchangeRatesService.getExchangeRate(accountFrom.getCurrency(), accountTo.getCurrency()))
                .thenReturn(exchangeRate);
        when(accountService.decreaseAccountBalance(accountTo, amount * exchangeRate)).thenReturn(true);


        assertThrows(ForbiddenException.class, () -> transactionService.userRespondsToRequest(createdUserFrom.getUsername(), transactionId, actionDto));

    }

    @Test
    public void userShouldRespondsToRequestWithDeny() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Request;
        Double amount = 100D;
        Double exchangeRate = 1.2;

        UserCreationRequestDto userCreationDto1 = new UserCreationRequestDto("user_test1", "user_test1", "ROLE_USER");
        User createdUserFrom = new User(userCreationDto1.getUsername(), "user_test1", userCreationDto1.getRoles());
        createdUserFrom.setId(1L);

        UserCreationRequestDto userCreationDto2 = new UserCreationRequestDto("user_test2", "user_test2", "ROLE_USER");
        User createdUserTo = new User(userCreationDto2.getUsername(), "user_test2", userCreationDto2.getRoles());
        createdUserTo.setId(2L);

        Account accountFrom = new Account(Currency.USD);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountFrom.setUser(createdUserFrom);

        Account accountTo = new Account(Currency.EUR);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);
        accountTo.setUser(createdUserTo);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), exchangeRate, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        accountTo.setTransactionsReceived(transactions);

        Set<Account> accountsFrom = new HashSet<>(Collections.singleton(accountFrom));
        createdUserFrom.setAccounts(accountsFrom);
        Set<Account> accountsTo = new HashSet<>(Collections.singleton(accountTo));
        createdUserTo.setAccounts(accountsTo);

        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(),
                transaction.getAmount(),
                AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));

        ActionDto actionDto = new ActionDto(ActionDto.Action.Deny);

        when(userService.getUser(createdUserFrom.getUsername())).thenReturn(Optional.of(createdUserTo));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(exchangeRatesService.getExchangeRate(accountFrom.getCurrency(), accountTo.getCurrency()))
                .thenReturn(exchangeRate);
        when(accountService.decreaseAccountBalance(accountTo, amount * exchangeRate)).thenReturn(true);


        TransactionResponseDto responseDto = transactionService.userRespondsToRequest(createdUserFrom.getUsername(), transactionId, actionDto);

        assertEquals(Transaction.State.Denied, transaction.getState());
        assertEquals(exchangeRate, transaction.getExchangeRate());
        assertNotNull(transaction.getTime());

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void shouldThrowsInvalidAmountExceptionWhenTheAccountHasNomoney() {
        Long accountId = 1L;
        Long transactionId = 1L;
        Double initialBalance = 100D;
        Transaction.Type type = Transaction.Type.Request;
        Double amount = 450D;
        Double exchangeRate = 1.2;

        UserCreationRequestDto userCreationDto1 = new UserCreationRequestDto("user_test1", "user_test1", "ROLE_USER");
        User createdUserFrom = new User(userCreationDto1.getUsername(), "user_test1", userCreationDto1.getRoles());
        createdUserFrom.setId(1L);

        UserCreationRequestDto userCreationDto2 = new UserCreationRequestDto("user_test2", "user_test2", "ROLE_USER");
        User createdUserTo = new User(userCreationDto2.getUsername(), "user_test2", userCreationDto2.getRoles());
        createdUserTo.setId(2L);

        Account accountFrom = new Account(Currency.USD);
        accountFrom.setId(accountId);
        accountFrom.setBalance(initialBalance);
        accountFrom.setUser(createdUserFrom);

        Account accountTo = new Account(Currency.EUR);
        accountTo.setId(accountId + 1);
        accountTo.setBalance(initialBalance);
        accountTo.setUser(createdUserTo);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(type, amount, LocalDateTime.now(), exchangeRate, accountFrom, accountTo);
        transaction.setId(transactionId);
        transactions.add(transaction);
        accountFrom.setTransactionsSent(transactions);
        accountTo.setTransactionsReceived(transactions);

        Set<Account> accountsFrom = new HashSet<>(Collections.singleton(accountFrom));
        createdUserFrom.setAccounts(accountsFrom);
        Set<Account> accountsTo = new HashSet<>(Collections.singleton(accountTo));
        createdUserTo.setAccounts(accountsTo);

        TransactionRequestDto requestDto = new TransactionRequestDto(transaction.getType(),
                transaction.getAmount(),
                AccountIdDto.fromAccount(transaction.getFrom()),
                AccountIdDto.fromAccount(transaction.getTo()));

        ActionDto actionDto = new ActionDto(ActionDto.Action.Accept);

        when(userService.getUser(createdUserFrom.getUsername())).thenReturn(Optional.of(createdUserTo));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(exchangeRatesService.getExchangeRate(accountFrom.getCurrency(), accountTo.getCurrency()))
                .thenReturn(exchangeRate);
        when(accountService.decreaseAccountBalance(accountTo, amount * exchangeRate)).thenReturn(false);


        assertThrows(InvalidAmountException.class, () -> transactionService.userRespondsToRequest(createdUserFrom.getUsername(), transactionId, actionDto));

    }
}