package com.fmi.rev.app.resource;

import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.dto.user.AuthenticationDto;
import com.fmi.rev.app.dto.user.UserCreationRequestDto;
import com.fmi.rev.app.dto.user.UserCredentialsDto;
import com.fmi.rev.app.dto.user.UserResponseDto;
import com.fmi.rev.app.model.Currency;
import com.fmi.rev.app.model.Transaction;
import com.fmi.rev.app.service.UserService;
import com.fmi.rev.app.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserResource {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserCreationRequestDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationDto> createJwt(@RequestBody @Valid UserCredentialsDto userDto) {
        return ResponseEntity.ok().body(new AuthenticationDto(userService.authenticate(userDto)));
    }

    @GetMapping("/me")
    @Secured({"ROLE_USER"})
    public ResponseEntity<UserResponseDto> getUser() {
        return ResponseEntity.ok(userService.getUserDto(SecurityContextUtils.getCurrentUsername()));
    }

    @GetMapping("/me/accounts")
    @Secured({"ROLE_USER"})
    public ResponseEntity<List<AccountResponseDto>> getAccounts(
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) Double minBalance,
            @RequestParam(required = false) Double maxBalance) {
        return ResponseEntity.ok(userService.getAccountsOfUser(SecurityContextUtils.getCurrentUsername(), currency, minBalance, maxBalance));
    }

    @GetMapping("/me/accounts/{accountId}")
    @Secured({"ROLE_USER"})
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(userService.getAccountOfUser(SecurityContextUtils.getCurrentUsername(), accountId));
    }

    @GetMapping("/me/accounts/{accountId}/transactions")
    @Secured({"ROLE_USER"})
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) Transaction.Type type,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) Transaction.State state,
            @RequestParam(required = false) @DateTimeFormat LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat LocalDate untilDate,
            @RequestParam(required = false) Long fromAccount,
            @RequestParam(required = false) Long toAccount) {
        LocalDateTime fromTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime untilTime = untilDate == null ? null : untilDate.atStartOfDay();
        return ResponseEntity.ok(userService.getTransactionsOfUserAccount(SecurityContextUtils.getCurrentUsername(), accountId,
                type, minAmount, maxAmount, state, fromTime, untilTime, fromAccount, toAccount));
    }

    @GetMapping("/me/accounts/{accountId}/transactions/{transactionId}")
    @Secured({"ROLE_USER"})
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable Long accountId, @PathVariable Long transactionId) {
        return ResponseEntity.ok(userService.getTransactionOfUserAccount(SecurityContextUtils.getCurrentUsername(), accountId, transactionId));
    }
}
