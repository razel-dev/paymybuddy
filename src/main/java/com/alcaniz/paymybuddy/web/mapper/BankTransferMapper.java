package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface BankTransferMapper {

    // Entity -> DTO
    @Mapping(source = "account.id", target = "accountId")
    BankTransferDTO toDto(BankTransfer entity);

    // CreateDTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "account", source = "accountId")
    BankTransfer toEntity(BankTransferCreateDTO dto);

    @ObjectFactory
    default BankTransfer newBankTransfer(BankTransferCreateDTO dto) {
        return BankTransfer.empty();
    }

    default Account mapIdToAccount(Integer id) {
        return id == null ? null : Account.ref(id);
    }
}