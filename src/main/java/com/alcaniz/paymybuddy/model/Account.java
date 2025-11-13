package com.alcaniz.paymybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "accounts")
public class Account {

    @Builder(toBuilder = true)
    private Account(@NonNull User user, @NonNull String accountName, @NonNull String currency, BigDecimal balance) {
        this.user = user;
        this.accountName = accountName;
        this.currency = currency;
        this.balance = (balance != null) ? balance : BigDecimal.ZERO;
    }

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    

}