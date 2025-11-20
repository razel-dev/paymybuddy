package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface AccountMapper {

    // Entity -> DTO
    @Mapping(source = "user.id", target = "userId")
    AccountDTO toDto(Account entity);

    // CreateDTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)     // défaut DB = 0.00
    @Mapping(target = "createdAt", ignore = true)   // valeur générée DB
    @Mapping(target = "user", source = "userId", qualifiedByName = "idToUser")
    Account toEntity(AccountCreateDTO dto);

    @Named("idToUser")
    default User idToUser(Integer id) {
        if (id == null) {
            return null;
        }
        User u = new User();
        u.setId(id);
        return u;
    }
}