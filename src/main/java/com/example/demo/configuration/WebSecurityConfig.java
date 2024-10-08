package com.example.demo.configuration;

import com.example.demo.enumPackage.Role;
import com.example.demo.exception.CustomAccessDeniedHandler;
import com.example.demo.security.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Thêm JwtTokenFilter để xác thực mỗi yêu cầu dựa trên JWT token
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth
                            // Các endpoint không cần xác thực
                            .requestMatchers("/auth/**", "/favicon.ico").permitAll()
                            // Bảo vệ các trang dành cho người dùng đã đăng nhập với quyền thích hợp
                            .requestMatchers("/user/**").hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name())
                            .anyRequest().authenticated(); // Yêu cầu xác thực cho mọi yêu cầu khác
                })
                // Xử lý quyền truy cập bị từ chối
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }
}