package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AccountService {

  Account create(@Valid AccountCreateDTO dto);

  Optional<Account> getById(Integer id);

  List<Account> getAllForUser(Integer userId);

  void deleteById(Integer id);
}