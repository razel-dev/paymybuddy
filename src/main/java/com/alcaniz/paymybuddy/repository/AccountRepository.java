package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Comptes d’un utilisateur
    List<Account> findAllByUser_Id(Integer userId);

}
