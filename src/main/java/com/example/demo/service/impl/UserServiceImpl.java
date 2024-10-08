package com.example.demo.service.impl;

import com.example.demo.dto.request.SaveUserRequestDTO;
import com.example.demo.dto.request.UpdateUserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // Lấy user theo ID
    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Lấy role theo name (thay cho roleId)
    private Role getRoleByName(String roleName) {
        return roleRepository.findRoleByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    @Override
    public long saveUser(SaveUserRequestDTO userRequestDTO) {
        // Kiểm tra xem email hoặc username đã tồn tại chưa
        List<User> existUsers = userRepository.findByUsernameOrEmail(userRequestDTO.getUsername(), userRequestDTO.getEmail());
        if (!existUsers.isEmpty()) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        // Thay đổi: Lấy Role theo tên role thay vì ID
        Role role = getRoleByName(userRequestDTO.getRole());

        // Tạo User mới
        User userEntity = User.builder()
                .username(userRequestDTO.getUsername())
                .email(userRequestDTO.getEmail())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .status(userRequestDTO.getStatus())
                .role(role) // Gán role dựa trên tên
                .build();

        // Lưu User và trả về ID
        User savedUser = userRepository.save(userEntity);
        return savedUser.getId();
    }

    @Override
    public void updateUser(long userId, UpdateUserRequestDTO userRequestDTO) {
        User userUpdate = getUserById(userId);

        // Cập nhật email nếu thay đổi
        if (userRequestDTO.getEmail() != null && !userRequestDTO.getEmail().equals(userUpdate.getEmail())) {
            Optional<User> userWithEmail = Optional.ofNullable(userRepository.findByEmail(userRequestDTO.getEmail()));
            if (userWithEmail.isPresent() && userWithEmail.get().getId() != userId) {
                throw new IllegalArgumentException("Email is already in use by another user");
            }
            userUpdate.setEmail(userRequestDTO.getEmail());
        }

        // Cập nhật password nếu thay đổi
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().equals(userUpdate.getPassword())) {
            String encodedPassword = passwordEncoder.encode(userRequestDTO.getPassword());
            userUpdate.setPassword(encodedPassword);
        }

        // Cập nhật status nếu thay đổi
        if (userRequestDTO.getStatus() != null && !userRequestDTO.getStatus().equals(userUpdate.getStatus())) {
            userUpdate.setStatus(userRequestDTO.getStatus());
        }

        // Cập nhật role nếu thay đổi (lấy role theo tên)
        if (userRequestDTO.getRole() != null && !userRequestDTO.getRole().equals(userUpdate.getRole().getName())) {
            Role role = getRoleByName(userRequestDTO.getRole());
            userUpdate.setRole(role); // Cập nhật role theo tên
        }

        userRepository.save(userUpdate);
    }

    @Override
    public void changeStatus(long userId, String userStatus) {
        User user = getUserById(userId);
        user.setStatus(userStatus.toLowerCase());
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO getUser(long userId) {
        User user = getUserById(userId);
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public Page<UserResponseDTO> getUsers(long page, long size, String sortBy, String sortDirection) {
        if (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
            sortDirection = "ASC";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of((int) page, (int) size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(userMapper::toUserResponseDTO);
    }
}