package com.fmi.rev.app.resource;

import com.fmi.rev.app.dto.account.AccountRequestDto;
import com.fmi.rev.app.dto.account.AccountResponseDto;
import com.fmi.rev.app.service.AccountService;
import com.fmi.rev.app.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
@Secured({"ROLE_USER"})
public class AccountResource {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> addNewAccountToUser(@RequestBody @Valid AccountRequestDto accountDto) {
        return ResponseEntity.ok(accountService.addNewAccountToUser(SecurityContextUtils.getCurrentUsername(), accountDto));
    }
}
