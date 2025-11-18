package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;


@Mapper(config = MapstructConfig.class)
public interface UserMapper {

    UserDTO toDto(User entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // hashé/posé côté service
    @Mapping(target = "createdAt", ignore = true) // généré DB
    User toEntity(UserCreateDTO dto);

    @ObjectFactory
    default User newUser(UserCreateDTO dto) {
        return User.empty();
    }
}