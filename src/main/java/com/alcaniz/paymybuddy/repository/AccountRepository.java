package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {


    List<Account> findAllByUser_Id(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByIdForUpdate(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findFirstByUser_EmailOrderByIdAscForUpdate(String email);

    Optional<Account> findFirstByUser_EmailOrderByIdAsc(String email);
}
