package com.example.demo.service.impl;

import com.example.demo.dto.request.LoginRequestDTO;
import com.example.demo.dto.response.LoginResponseDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.enumPackage.Status;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {

        User user = userRepository.findByUsername(loginRequestDTO.getUsername());
        if (user == null) {
            throw new UnauthorizedException("Login failed! User not found.");
        }

        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

            // Authentication with the authentication manager
            authenticationManager.authenticate(authenticationToken);

            if (!user.getStatus().equals(Status.ACTIVE.getValue())) {
                throw new UnauthorizedException("Login failed! User not active.");
            }

            // Generate the JWT token if authentication was successful
            String token = jwtTokenUtil.generateToken(user);

            // Create a cookie with the generated token
            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(true); // Giúp ngăn chặn truy cập từ JavaScript (tăng cường bảo mật)
            jwtCookie.setSecure(true); // Chỉ cho phép truyền cookie qua HTTPS
            jwtCookie.setPath("/"); // Cookie có hiệu lực cho toàn bộ ứng dụng
            jwtCookie.setMaxAge(24 * 60 * 60); // Cookie hết hạn sau 24 giờ

            // Add cookie to the response
            response.addCookie(jwtCookie);

            // Return login response with token (optional)
            return LoginResponseDTO.builder().token(token).build();

        } catch (Exception e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Override
    public UserResponseDTO getCurrentUser(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .role(user.getRole().getName())  // Thay đổi từ roleId thành role name
                .build();
    }
}