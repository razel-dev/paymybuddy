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
@Table(name = "transactions")
public class Transaction {


@Builder(toBuilder = true)
private Transaction(@NonNull Account senderAccount, @NonNull Account receiverAccount, String description, @NonNull BigDecimal amount, @NonNull BigDecimal fee) {
    this.senderAccount = senderAccount;
    this.receiverAccount = receiverAccount;
    this.description = description;
    this.amount = amount;
    this.fee = fee;
}

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private Account receiverAccount;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}