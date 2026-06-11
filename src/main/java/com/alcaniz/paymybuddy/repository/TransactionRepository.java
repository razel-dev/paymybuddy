package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    //  historique simple
    List<Transaction> findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
            Integer senderAccountId, Integer receiverAccountId
    );

    long countBySenderAccount_User_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Integer senderUserId,
            Instant dayStart,
            Instant dayEnd
    );

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.senderAccount.user.id = :senderUserId
              and t.createdAt >= :dayStart
              and t.createdAt < :dayEnd
            """)
    BigDecimal sumAmountBySenderUserIdAndCreatedAtBetween(
            @Param("senderUserId") Integer senderUserId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );
}
