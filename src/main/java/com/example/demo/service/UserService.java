package com.example.demo.service;

import com.example.demo.dto.request.SaveUserRequestDTO;
import com.example.demo.dto.request.UpdateUserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;

public interface UserService {
    long saveUser(SaveUserRequestDTO user);

    void updateUser(long userId, UpdateUserRequestDTO user);

    void changeStatus(long userId, String userStatus);

    UserResponseDTO getUser(long userId);

    Page<UserResponseDTO> getUsers(long page, long size, String sortBy, String sortDirection);
}