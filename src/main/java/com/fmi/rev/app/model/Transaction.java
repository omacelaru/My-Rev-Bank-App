package com.fmi.rev.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
public class Transaction {
    public enum Type {
        Deposit,
        Withdrawal,
        Payment,
        Request
    }

    public enum State {
        Completed,
        Pending,
        Accepted,
        Denied
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Type type;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private State state;
    @Column(nullable = false)
    private LocalDateTime time;
    @Column(nullable = false)
    private Double exchangeRate;
    @ManyToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(nullable = false)
    private Account from;
    @ManyToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(nullable = false)
    private Account to;

    public Transaction(Type type, Double amount, LocalDateTime time, Double exchangeRate, Account from, Account to) {
        this.type = type;
        this.amount = amount;
        this.state = type == Type.Request ? State.Pending : State.Completed;
        this.time = time;
        this.exchangeRate = exchangeRate;
        this.from = from;
        this.to = to;
    }
}
