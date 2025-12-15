package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface TransactionMapper {

    // Entity -> DTO
    @Mapping(source = "senderAccount.id", target = "senderAccountId")
    @Mapping(source = "receiverAccount.id", target = "receiverAccountId")
    @Mapping(source = "receiverAccount.user.email", target = "receiverEmail")
    TransactionDTO toDto(Transaction entity);

    // CreateDTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "fee", ignore = true) // calcul métier côté service
    @Mapping(target = "senderAccount.id", source = "senderAccountId")
    @Mapping(target = "receiverAccount", ignore = true) // résolu par le service depuis receiverEmail
    Transaction toEntity(TransactionCreateDTO dto);

}