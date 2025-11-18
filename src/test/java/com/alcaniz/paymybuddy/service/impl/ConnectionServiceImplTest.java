package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.impl.ConnectionServiceImpl;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import com.alcaniz.paymybuddy.web.mapper.UserConnectionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// création heureuse, listing (null -> vide + délégation), suppression idempotente, existence.
@ExtendWith(MockitoExtension.class)
class ConnectionServiceImplTest {

    @Mock UserConnectionRepository repo;
    @Mock UserRepository userRepo;
    @Mock UserConnectionMapper mapper;

    @InjectMocks
    ConnectionServiceImpl service;

    @Test
    void create() {
        // Arrange
        var dto = new UserConnectionDTO(1, 2);
        when(userRepo.findById(1)).thenReturn(Optional.of(mock(User.class)));
        when(userRepo.findById(2)).thenReturn(Optional.of(mock(User.class)));
        when(repo.existsByOwner_IdAndRelated_Id(1, 2)).thenReturn(false);
        UserConnection mapped = mock(UserConnection.class); // évite problème de constructeur protégé
        when(mapper.toEntity(dto)).thenReturn(mapped);
        UserConnection saved = mock(UserConnection.class);
        when(repo.save(mapped)).thenReturn(saved);

        // Act
        UserConnection res = service.create(dto);

        // Assert
        assertNotNull(res);
        verify(repo).save(mapped);
    }

    @Test
    void getAllForOwner() {
        // null -> liste vide sans appel repo
        assertTrue(service.getAllForOwner(null).isEmpty());
        verify(repo, never()).findAllByOwner_Id(any());

        // appel nominal
        when(repo.findAllByOwner_Id(7)).thenReturn(Collections.emptyList());
        List<?> list = service.getAllForOwner(7);
        assertNotNull(list);
        verify(repo).findAllByOwner_Id(7);
    }

    @Test
    void delete() {
        // inexistante -> pas de delete
        when(repo.existsByOwner_IdAndRelated_Id(1, 2)).thenReturn(false);
        service.delete(1, 2);
        verify(repo, never()).deleteByOwner_IdAndRelated_Id(anyInt(), anyInt());

        // existante -> delete appelé
        when(repo.existsByOwner_IdAndRelated_Id(3, 4)).thenReturn(true);
        service.delete(3, 4);
        verify(repo).deleteByOwner_IdAndRelated_Id(3, 4);

        // ids nuls -> idempotent
        service.delete(null, 2);
        service.delete(1, null);
        verify(repo, atMostOnce()).deleteByOwner_IdAndRelated_Id(3, 4);
    }

    @Test
    void exists() {
        // null -> false
        assertFalse(service.exists(null, 2));
        assertFalse(service.exists(1, null));

        // délégation repo
        when(repo.existsByOwner_IdAndRelated_Id(5, 6)).thenReturn(true);
        assertTrue(service.exists(5, 6));
        verify(repo).existsByOwner_IdAndRelated_Id(5, 6);
    }
}