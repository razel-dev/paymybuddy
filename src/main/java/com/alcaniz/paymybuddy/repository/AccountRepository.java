package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Comptes d’un utilisateur
    List<Account> findAllByUser_Id(Integer userId);

}
