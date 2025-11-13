package com.alcaniz.paymybuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bank_transfers")
public class BankTransfer {

    public enum TransferType { DEPOSIT, WITHDRAWAL }

    @Builder(toBuilder = true)
    private BankTransfer(@NonNull Account account, @NonNull BigDecimal amount, @NonNull TransferType type) {
        this.account = account;
        this.amount = amount;
        this.type = type;
    }
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransferType type;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}