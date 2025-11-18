package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
public interface BankTransferService {

    BankTransfer create(@Valid BankTransferCreateDTO dto);

    List<BankTransfer> getHistoryForAccount(Integer accountId);

    Optional<BankTransfer> getById(Integer id);
}