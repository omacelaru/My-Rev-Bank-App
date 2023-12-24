package com.fmi.rev.app.dto.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActionDto {
    public enum Action {
        Accept,
        Deny
    }

    @NotNull
    private Action action;
}
