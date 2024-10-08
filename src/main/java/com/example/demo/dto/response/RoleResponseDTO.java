package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class RoleResponseDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
}