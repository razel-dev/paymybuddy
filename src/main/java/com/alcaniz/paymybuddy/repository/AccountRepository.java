package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {


    List<Account> findAllByUser_Id(Integer userId);


    Optional<Account> findFirstByUser_EmailOrderByIdAsc(String email);
}
