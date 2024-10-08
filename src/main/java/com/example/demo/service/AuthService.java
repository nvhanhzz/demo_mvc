package com.example.demo.service;

import com.example.demo.dto.request.LoginRequestDTO;
import com.example.demo.dto.response.LoginResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);

    UserResponseDTO getCurrentUser(User user);
}