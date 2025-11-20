package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface TransactionMapper {

    // Entity -> DTO
    @Mapping(source = "senderAccount.id", target = "senderAccountId")
    @Mapping(source = "receiverAccount.id", target = "receiverAccountId")
    TransactionDTO toDto(Transaction entity);

    // CreateDTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "fee", ignore = true) // calcul métier côté service
    @Mapping(target = "senderAccount.id", source = "senderAccountId")
    @Mapping(target = "receiverAccount.id", source = "receiverAccountId")
    Transaction toEntity(TransactionCreateDTO dto);

}