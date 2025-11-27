package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
public interface BankTransferService {

    BankTransferDTO create(@Valid BankTransferCreateDTO dto);

    List<BankTransferDTO> getHistoryForAccount(Integer accountId);

    Optional<BankTransferDTO> getById(Integer id);
}