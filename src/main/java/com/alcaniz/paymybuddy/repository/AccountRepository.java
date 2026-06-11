package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {


    List<Account> findAllByUser_Id(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Account> findAllByIdInOrderByIdAsc(List<Integer> ids);

    Optional<Account> findFirstByUser_EmailAndAccountNameOrderByIdAsc(String email, String accountName);

    Optional<Account> findFirstByUser_EmailOrderByIdAsc(String email);
}
