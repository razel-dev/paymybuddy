package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.impl.UserServiceImpl;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl service;

    @Test
    void create() {
        // Arrange
        UserCreateDTO dto = new UserCreateDTO("  Alice  ", "  Alice@Mail.com  ", "secretPass!");
        when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secretPass!")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42);
            return u;
        });

        // Act
        User created = service.create(dto);

        // Assert
        assertNotNull(created);
        assertEquals(42, created.getId());
        assertEquals("Alice", created.getUsername());
        assertEquals("alice@mail.com", created.getEmail());
        assertEquals("HASHED", created.getPassword());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).existsByEmail("alice@mail.com");
        verify(passwordEncoder).encode("secretPass!");
        verify(userRepository).save(captor.capture());
        assertEquals("Alice", captor.getValue().getUsername());
        assertEquals("alice@mail.com", captor.getValue().getEmail());

        // Sanity checks erreurs fréquentes
        assertThrows(BadRequestException.class, () -> service.create(null));
        assertThrows(BusinessException.class, () -> {
            when(userRepository.existsByEmail("user@mail.com")).thenReturn(true);
            service.create(new UserCreateDTO("User", "user@mail.com", "password123"));
        });
    }

    @Test
    void getById() {
        // Arrange
        User u = User.builder().username("Bob").email("bob@mail.com").password("x").build();
        u.setId(7);
        when(userRepository.findById(7)).thenReturn(Optional.of(u));

        // Act + Assert (id non nul)
        Optional<User> found = service.getById(7);
        assertTrue(found.isPresent());
        assertEquals(7, found.get().getId());
        verify(userRepository).findById(7);

        // Act + Assert (id nul -> empty, pas d'appel repo)
        assertTrue(service.getById(null).isEmpty());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getByEmail() {
        // Arrange
        User u = User.builder().username("Eve").email("eve@mail.com").password("x").build();
        u.setId(9);
        when(userRepository.findByEmail("eve@mail.com")).thenReturn(Optional.of(u));

        // Act + Assert (normalisation trim + lower-case)
        Optional<User> res = service.getByEmail("  EVE@mail.com  ");
        assertTrue(res.isPresent());
        assertEquals("eve@mail.com", res.get().getEmail());
        verify(userRepository).findByEmail("eve@mail.com");

        // Act + Assert (email vide -> empty, pas d'appel repo)
        assertTrue(service.getByEmail("   ").isEmpty());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void isEmailAvailable() {
        // Arrange
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);

        // Act + Assert
        assertTrue(service.isEmailAvailable("  John@mail.com "));
        verify(userRepository).existsByEmail("john@mail.com");

        // Cas email vide -> false et aucun appel repo supplémentaire
        assertFalse(service.isEmailAvailable("   "));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteById() {
        // id nul -> idempotent, aucun appel repo
        service.deleteById(null);
        verifyNoInteractions(userRepository);

        // utilisateur inexistant -> idempotent
        when(userRepository.existsById(123)).thenReturn(false);
        service.deleteById(123);
        verify(userRepository).existsById(123);
        verify(userRepository, never()).deleteById(anyInt());

        // utilisateur existant -> suppression
        when(userRepository.existsById(5)).thenReturn(true);
        service.deleteById(5);
        verify(userRepository).existsById(5);
        verify(userRepository).deleteById(5);
    }
}