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
    @Mapping(target = "account", source = "accountId", qualifiedByName = "idToAccount")
    BankTransfer toEntity(BankTransferCreateDTO dto);

    @Named("idToAccount")
    default Account idToAccount(Integer id) {
        if (id == null) {
            return null;
        }
        Account a = new Account();
        a.setId(id);
        return a;
    }
}