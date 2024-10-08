package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateUserRequestDTO implements Serializable {

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;

    @Pattern(regexp = "^(active|inactive)$", message = "Status must be 'active' or 'inactive'")
    private String status;

    @NotBlank(message = "Role is required")
    @Size(min = 3, max = 50, message = "Role must be between 3 and 50 characters") // Thêm điều kiện cho chuỗi role
    private String role; // Thay đổi từ roleId thành role (dạng String để lưu tên của role)
}