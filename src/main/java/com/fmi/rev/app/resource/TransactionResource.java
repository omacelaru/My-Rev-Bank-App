package com.fmi.rev.app.resource;

import com.fmi.rev.app.dto.transaction.ActionDto;
import com.fmi.rev.app.dto.transaction.TransactionRequestDto;
import com.fmi.rev.app.dto.transaction.TransactionResponseDto;
import com.fmi.rev.app.service.TransactionService;
import com.fmi.rev.app.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
@Secured({"ROLE_USER"})
public class TransactionResource {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> makeTransaction(@RequestBody @Valid TransactionRequestDto transactionDto) {
        return ResponseEntity.ok(transactionService.userMakesTransaction(SecurityContextUtils.getCurrentUsername(), transactionDto));
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> respondToRequest(@PathVariable Long transactionId, @RequestBody @Valid ActionDto actionDto) {
        return ResponseEntity.ok(transactionService.userRespondsToRequest(SecurityContextUtils.getCurrentUsername(), transactionId, actionDto));
    }
}
