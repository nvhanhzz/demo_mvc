package com.example.demo.configuration;

import com.example.demo.enumPackage.Status;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class CreatedAccountConfig {

    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Bean
    ApplicationRunner createAccount(UserRepository userRepository) {
        return args -> {
            if(userRepository.findByUsername("hanhnv") == null) {
                Role role =  roleRepository.findRoleByName("ADMIN").orElseThrow(() ->  new ResourceNotFoundException("Role not found"));
                User user = User.builder()
                        .username("hanhnv")
                        .password(passwordEncoder.encode("123456"))
                        .role(role)
                        .email("hanhnv@gmail.com")
                        .status(Status.ACTIVE.getValue())
                        .build();
                userRepository.save(user);
                log.info("Created account successfully: {}", user.getUsername());
            }
        };
    }
}
