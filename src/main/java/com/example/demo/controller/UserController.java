package com.example.demo.controller;

import com.example.demo.dto.request.SaveUserRequestDTO;
import com.example.demo.dto.request.UpdateUserRequestDTO;
import com.example.demo.dto.response.UserResponseDTO;
import com.example.demo.enumPackage.Status;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleRepository roleRepository;

    // Trang hiển thị thông tin chi tiết của người dùng
    @GetMapping("/{userId}")
    public String getUser(@PathVariable long userId, Model model) {
        UserResponseDTO user = userService.getUser(userId);
        model.addAttribute("user", user);
        return "userDetails"; // Trả về view userDetails.html để hiển thị thông tin người dùng
    }

    // Trang thêm người dùng mới
    @GetMapping("/add")
    public String addUserPage(Model model) {
        model.addAttribute("userDTO", new SaveUserRequestDTO());

        // Lấy danh sách các role từ backend
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles); // Truyền danh sách role vào model

        return "addUser";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("userDTO") SaveUserRequestDTO userDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi validate, trả về form với thông báo lỗi
            return "addUser";
        }

        try {
            userService.saveUser(userDTO);
            return "redirect:/user"; // Chuyển hướng đến danh sách người dùng sau khi thêm thành công
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add user: " + e.getMessage());
            return "addUser"; // Trả về lại form nếu thêm thất bại
        }
    }

    // Trang danh sách người dùng
    @GetMapping("")
    public String getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Model model) {

        // Lấy danh sách người dùng theo phân trang
        Page<UserResponseDTO> users = userService.getUsers(page, size, sortBy, sortDirection);

        // Truyền dữ liệu phân trang và người dùng vào model
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("pageSize", size);

        return "listUser"; // Trả về view userList.html để hiển thị danh sách người dùng
    }

    // Trang chỉnh sửa thông tin người dùng
    @GetMapping("/{userId}/edit")
    public String editUserPage(@PathVariable long userId, Model model) {
        UserResponseDTO user = userService.getUser(userId);
        UpdateUserRequestDTO userDTO = new UpdateUserRequestDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setStatus(user.getStatus().toLowerCase());
        userDTO.setRole(user.getRole()); // Gán tên role hiện tại

        // Lấy danh sách các role từ database
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles); // Truyền danh sách role vào model
        model.addAttribute("userDTO", userDTO);
        return "editUser";
    }


    @PostMapping("/{userId}/edit")
    public String updateUser(@PathVariable long userId, @Valid @ModelAttribute("userDTO") UpdateUserRequestDTO userDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi validate, trả về form với thông báo lỗi
            return "editUser";
        }

        try {
            userService.updateUser(userId, userDTO);
            return "redirect:/user"; // Chuyển hướng đến danh sách người dùng sau khi cập nhật thành công
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update user: " + e.getMessage());
            return "editUser"; // Trả về lại form nếu cập nhật thất bại
        }
    }

    // Xử lý yêu cầu thay đổi trạng thái người dùng từ danh sách người dùng
    @PostMapping("/{userId}/status")
    public String updateUserStatus(@PathVariable long userId, @RequestParam String status, Model model) {
        try {
            // Kiểm tra tính hợp lệ của trạng thái
            Status.validate(status);
            userService.changeStatus(userId, status);
            return "redirect:/user"; // Chuyển hướng đến danh sách người dùng sau khi cập nhật trạng thái thành công
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update user status: " + e.getMessage());
            return "listUser"; // Trả về trang danh sách người dùng với thông báo lỗi nếu cập nhật thất bại
        }
    }
}