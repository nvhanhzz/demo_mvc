package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class UserResponseDTO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String status;
    private String role; // Thay đổi từ roleId thành role dạng String để lưu name
}