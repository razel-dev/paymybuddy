package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AccountService {

  AccountDTO create(@Valid AccountCreateDTO dto);

  Optional<AccountDTO> getById(Integer id);

  List<AccountDTO> getAllForUser(Integer userId);

  void deleteById(Integer id);
}