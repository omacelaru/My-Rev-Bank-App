package com.fmi.rev.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@NoArgsConstructor
@Data
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Currency currency;
    @Column(nullable = false)
    private Double balance;
    @ManyToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(nullable = false)
    private User user;
    @OneToMany(mappedBy = "from", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Transaction> transactionsSent = new HashSet<>();
    @OneToMany(mappedBy = "to", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Transaction> transactionsReceived = new HashSet<>();

    public Account(Currency currency) {
        this.currency = currency;
        this.balance = 0.0;
    }

    public Set<Transaction> getTransactions() {
        Set<Transaction> transactions = new HashSet<>();
        Stream.of(transactionsSent, transactionsReceived).forEach(transactions::addAll);
        return transactions;
    }

    public void increaseBalance(Double amount) {
        balance += amount;
    }

    public boolean decreaseBalance(Double amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
}
