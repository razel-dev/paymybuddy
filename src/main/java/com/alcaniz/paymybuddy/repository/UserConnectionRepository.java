package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.model.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, UserConnectionId> {

    List<UserConnection> findAllByOwner_Id(Integer ownerUserId);

    boolean existsByOwner_IdAndRelated_Id(Integer ownerUserId, Integer relatedUserId);

    void deleteByOwner_IdAndRelated_Id(Integer ownerUserId, Integer relatedUserId);
}