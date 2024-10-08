package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class LoginResponseDTO implements Serializable {
    private String token;
}