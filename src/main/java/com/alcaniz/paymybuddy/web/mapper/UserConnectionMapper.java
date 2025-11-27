package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.web.dto.connection.ConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructConfig.class)
public interface UserConnectionMapper {

    // Entity -> DTO de sortie (API)
    @Mapping(source = "owner.id", target = "ownerUserId")
    @Mapping(source = "related.id", target = "relatedUserId")
    ConnectionDTO toDto(UserConnection entity);

    // DTO d’entrée -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "owner", source = "ownerUserId", qualifiedByName = "idToUser")
    @Mapping(target = "related", source = "relatedUserId", qualifiedByName = "idToUser")
    UserConnection toEntity(UserConnectionDTO dto);

    @Named("idToUser")
    default User idToUser(Integer id) {
        if (id == null) return null;
        User u = new User();
        u.setId(id);
        return u;
    }
}