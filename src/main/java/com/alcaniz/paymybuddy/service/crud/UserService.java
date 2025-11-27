package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import jakarta.validation.Valid;
import java.util.Optional;

public interface UserService {

    UserDTO create(@Valid UserCreateDTO dto);

    Optional<UserDTO> getById(Integer id);

    Optional<UserDTO> getByEmail(String email);

    boolean isEmailAvailable(String email);

    void deleteById(Integer id);
}