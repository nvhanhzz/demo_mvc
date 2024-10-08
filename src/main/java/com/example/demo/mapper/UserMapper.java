package com.example.demo.mapper;

import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role.name", target = "role")
    UserResponseDTO toUserResponseDTO(User user);
}