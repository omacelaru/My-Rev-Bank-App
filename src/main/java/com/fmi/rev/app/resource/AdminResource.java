package com.fmi.rev.app.resource;

import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.dto.user.UserResponseDto;
import com.fmi.rev.app.service.AccountService;
import com.fmi.rev.app.service.TransactionService;
import com.fmi.rev.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@Secured({"ROLE_ADMIN"})
public class AdminResource {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserDto(userId));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponseDto>> getAccounts() {
        return ResponseEntity.ok(accountService.getAccounts());
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountDto(accountId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionDto(transactionId));
    }
}
