package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.model.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserConnectionRepository extends JpaRepository<UserConnection, UserConnectionId> {

    List<UserConnection> findAllByOwner_Id(Integer ownerUserId);

    boolean existsByOwner_IdAndRelated_Id(Integer ownerUserId, Integer relatedUserId);
}