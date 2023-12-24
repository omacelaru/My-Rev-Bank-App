package com.fmi.rev.app.service;

import com.fmi.rev.app.dto.account.AccountRequestDto;
import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.exception.AccountNotFoundException;
import com.fmi.rev.app.exception.UnauthorizedException;
import com.fmi.rev.app.repository.AccountRepository;
import com.fmi.rev.app.model.Account;
import com.fmi.rev.app.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }

    public AccountResponseDto getAccountDto(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(AccountNotFoundException::new);
        return AccountResponseDto.fromAccount(account);
    }

    public List<AccountResponseDto> getAccounts() {
        return accountRepository.findAll().stream().map(AccountResponseDto::fromAccount).toList();
    }

    public AccountResponseDto addNewAccountToUser(String username, AccountRequestDto accountDto) {
        User user = userService.getUser(username).orElseThrow(UnauthorizedException::new);
        Account account = new Account(accountDto.getCurrency());
        account.setUser(user);
        accountRepository.save(account);
        return AccountResponseDto.fromAccount(account);
    }

    public void increaseAccountBalance(Account account, Double amount) {
        account.increaseBalance(amount);
        accountRepository.save(account);
    }

    public boolean decreaseAccountBalance(Account account, Double amount) {
        if (account.decreaseBalance(amount)) {
            accountRepository.save(account);
            return true;
        }
        return false;
    }
}
