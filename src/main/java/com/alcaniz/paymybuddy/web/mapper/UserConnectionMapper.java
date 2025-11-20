package com.alcaniz.paymybuddy.web.mapper;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.model.UserConnectionId;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface UserConnectionMapper {

    // Entity -> DTO
    @Mapping(source = "owner.id", target = "ownerUserId")
    @Mapping(source = "related.id", target = "relatedUserId")
    UserConnectionDTO toDto(UserConnection entity);

    // DTO -> Entity
    @Mapping(target = "createdAt", ignore = true)
    // remplir à la fois l'EmbeddedId et les associations @MapsId
    @Mapping(target = "id.ownerUserId", source = "ownerUserId")
    @Mapping(target = "id.relatedUserId", source = "relatedUserId")
    @Mapping(target = "owner", source = "ownerUserId", qualifiedByName = "idToUser")
    @Mapping(target = "related", source = "relatedUserId", qualifiedByName = "idToUser")
    UserConnection toEntity(UserConnectionDTO dto);

    // Factory EmbeddedId (évite d'appeler un ctor potentiellement protégé)
    @ObjectFactory
    default UserConnectionId newUserConnectionId(UserConnectionDTO dto) {
        return UserConnectionId.empty();
    }
    // Factory entité (évite new UserConnection() si ctor protégé)
    @ObjectFactory
    default UserConnection newUserConnection(@TargetType Class<UserConnection> type) {
        return UserConnection.empty();
    }

    // Conversion Integer -> User sans new User() côté mapper
    @Named("idToUser")
    default User mapIdToUser(Integer id) {
        return id == null ? null : User.ref(id);
    }
}