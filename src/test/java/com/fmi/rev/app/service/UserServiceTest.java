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
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.Currency;
import com.fmi.rev.app.model.Transaction;
import com.fmi.rev.app.model.User;
import com.fmi.rev.app.repository.UserRepository;
import com.fmi.rev.app.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    @InjectMocks
    private UserService userService;
    private static final long TTL = 60;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtProvider);
    }

    @Test
    public void testGetUser() {
        String userName = "user_test";

        userService.getUser(userName);

        verify(userRepository, times(1)).findUserByUsername(userName);
    }

    @Test
    public void shouldCreateUserWithAValidUserCreationRequestDto() {
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());

        UserResponseDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(createdUser.getUsername(), result.getUsername());
        assertEquals(createdUser.getRoles(), result.getRoles());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldAuthenticateAValidUserCredentials() {
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());
        String expectedToken = jwtProvider.generateToken(createdUser.getUsername(), TTL, Arrays.stream(createdUser.getRoles().split(",")).collect(Collectors.toSet()));
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(createdUser.getUsername(), createdUser.getPassword());

        when(passwordEncoder.matches(userCredentialsDto.getPassword(), encodedPassword)).thenReturn(true);
        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        String actualToken = userService.authenticate(userCredentialsDto);

        assertEquals(expectedToken, actualToken);
        verify(passwordEncoder, times(1)).matches(userCredentialsDto.getPassword(), encodedPassword);
    }

    @Test
    public void shouldThrowsUnauthorizedExceptionWhenAuthenticateWithAnInvalidPassword() {
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());
        UserCredentialsDto userCredentialsDto = new UserCredentialsDto(createdUser.getUsername(), createdUser.getPassword());

        when(passwordEncoder.matches(userCredentialsDto.getPassword(), encodedPassword)).thenReturn(false);
        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        assertThrows(UnauthorizedException.class, () -> userService.authenticate(userCredentialsDto));
    }

    @Test
    public void shouldGetUserDtoByUserName() {
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));
        UserResponseDto user = userService.getUserDto(createdUser.getUsername());

        assertEquals(UserResponseDto.fromUser(createdUser), user);
    }

    @Test
    public void shouldThrowsUnauthorizedExceptionWhenGetUserDtoByUserName() {
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());

        when(userService.getUser(createdUser.getUsername())).thenThrow(UnauthorizedException.class);

        assertThrows(UnauthorizedException.class, () -> userService.getUserDto(createdUser.getUsername()));
    }

    @Test
    public void shouldGetUserDtoById() {
        Long userId = 1L;
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());

        when(userRepository.findById(userId)).thenReturn(Optional.of(createdUser));
        userService.getUserDto(userId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void shouldThrowsUserNotFoundExceptionWhenGetUserDtoById() {
        Long userId = 1L;
        UserCreationRequestDto userCreationDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userCreationDto.getPassword());
        User createdUser = new User(userCreationDto.getUsername(), encodedPassword, userCreationDto.getRoles());

        when(userRepository.findById(userId)).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> userService.getUserDto(createdUser.getId()));
    }

    @Test
    public void shouldGetUsers() {
        userService.getUsers();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void shouldGetAccountsOfUser() {
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Currency currency = Currency.USD;
        Double minBalance = 100.0;
        Double maxBalance = 1000.0;
        Account account = new Account(Currency.USD);
        account.setUser(createdUser);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        List<AccountResponseDto> accounts = userService.getAccountsOfUser(createdUser.getUsername(), currency, minBalance, maxBalance);

        assertNotNull(accounts);
    }

    @Test
    public void shouldGetAccountOfUser() {
        Long accountId = 1L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(1L);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        AccountResponseDto responseDto = userService.getAccountOfUser(createdUser.getUsername(), accountId);

        assertNotNull(responseDto);
    }

    @Test
    public void shouldThrowsAccountNotFoundExceptionWhenGetAccountOfUser() {
        Long accountId = 2L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(1L);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        assertThrows(AccountNotFoundException.class, () -> userService.getAccountOfUser(createdUser.getUsername(), accountId));
    }

    @Test
    public void shouldGetTransactionOfUserAccount() {
        Long accountId = 1L;
        Long transactionId = 1L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(accountId);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(Transaction.Type.Deposit, 100.0, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        TransactionResponseDto responseDto = userService.getTransactionOfUserAccount(createdUser.getUsername(), accountId, transactionId);

        assertNotNull(responseDto);
        assertEquals(transactionId, responseDto.getId());
    }

    @Test
    public void shouldThrowsAccountNotFoundExceptionWhenGetTransactionOfUserAccount() {
        Long accountId = 2L;
        Long transactionId = 1L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(1L);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(Transaction.Type.Deposit, 100.0, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        assertThrows(AccountNotFoundException.class, () -> userService.getTransactionOfUserAccount(createdUser.getUsername(), accountId, transactionId));

    }

    @Test
    public void testGetTransactionsOfUserAccount() {
        Long accountId = 1L;
        Long transactionId = 1L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(1L);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(Transaction.Type.Deposit, 150.0, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(transactionId);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        Transaction.Type type = Transaction.Type.Deposit;
        Double minAmount = 100.0;
        Double maxAmount = 1000.0;
        Transaction.State state = Transaction.State.Completed;
        LocalDateTime fromTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime untilTime = LocalDateTime.of(2023, 6, 1, 0, 0);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        List<TransactionResponseDto> result = userService.getTransactionsOfUserAccount(
                createdUser.getUsername(), accountId, type, minAmount, maxAmount, state, fromTime, untilTime, accountId, accountId);
        assertNotNull(result);
    }

    @Test
    public void shouldThrowsTransactionNotFoundExceptionWhenGetTransactionOfUserAccount() {
        Long accountId = 1L;
        Long transactionId = 2L;
        UserCreationRequestDto userDto = new UserCreationRequestDto("user_test", "user_test", "ROLsE_USER");
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User createdUser = new User(userDto.getUsername(), encodedPassword, userDto.getRoles());
        Account account = new Account(Currency.USD);
        account.setId(accountId);

        Set<Transaction> transactions = new HashSet<>();
        Transaction transaction = new Transaction(Transaction.Type.Deposit, 100.0, LocalDateTime.now(), 1.0, account, account);
        transaction.setId(1L);
        transactions.add(transaction);
        account.setTransactionsSent(transactions);
        Set<Account> accounts = new HashSet<>(Collections.singleton(account));
        createdUser.setAccounts(accounts);

        when(userService.getUser(createdUser.getUsername())).thenReturn(Optional.of(createdUser));

        assertThrows(TransactionNotFoundException.class, () -> userService.getTransactionOfUserAccount(createdUser.getUsername(), accountId, transactionId));
    }
}