package com.fmi.rev.app.service;

import com.fmi.rev.app.dto.account.AccountRequestDto;
import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.exception.AccountNotFoundException;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.Currency;
import com.fmi.rev.app.model.User;
import com.fmi.rev.app.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final UserService userService = mock(UserService.class);
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountService(accountRepository, userService);
    }

    @Test
    public void testGetAccount() {
        Long accountId = 1L;

        accountService.getAccount(accountId);

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    public void shouldGetAccountDto() {
        Long accountId = 1L;

        Account account = new Account(Currency.USD);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.getAccountDto(accountId);

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    public void shouldThrowsAccountNotFoundExceptionWhenGetAccountDto() {
        Long invalidAccountId = 999L;

        when(accountRepository.findById(invalidAccountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountDto(invalidAccountId));
        verify(accountRepository, times(1)).findById(invalidAccountId);
    }

    @Test
    public void testGetAccounts() {
        accountService.getAccounts();

        verify(accountRepository, times(1)).findAll();
    }

    @Test
    public void shouldAddNewAccountToUser() {
        String userName = "user_test";
        User user = new User();
        AccountRequestDto accountDto = new AccountRequestDto(Currency.EUR);
        Account account = new Account(accountDto.getCurrency());
        AccountResponseDto userAccount = AccountResponseDto.fromAccount(account);

        when(userService.getUser(userName)).thenReturn(Optional.of(user));
        when(accountRepository.save(any())).thenReturn(account);

        AccountResponseDto result = accountService.addNewAccountToUser(userName, accountDto);

        assertEquals(userAccount, result);
    }

    @Test
    public void shouldIncreaseAccountBalance() {
        Account account = new Account();
        Double initialBalance = 200.0;
        Double amount = 100.0;
        account.setBalance(initialBalance);

        accountService.increaseAccountBalance(account, amount);

        assertEquals(amount, account.getBalance() - initialBalance);
        verify(accountRepository).save(account);
    }

    @Test
    public void shouldDecreaseAccountBalanceWhenThereIsSufficientBalance() {
        Account account = new Account();
        Double initialBalance = 200.0;
        Double amountToDecrease = 100.0;
        account.setBalance(initialBalance);

        boolean result = accountService.decreaseAccountBalance(account, amountToDecrease);

        assertTrue(result);
        assertEquals(initialBalance - amountToDecrease, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    public void nothingShouldHappenWhenThereIsInsufficientBalanceInTheAccount() {
        Account account = new Account();
        Double initialBalance = 50.0;
        Double amountToDecrease = 100.0;
        account.setBalance(initialBalance);

        boolean result = accountService.decreaseAccountBalance(account, amountToDecrease);

        assertFalse(result);
        assertEquals(initialBalance, account.getBalance());
        verify(accountRepository, never()).save(account);
    }
}