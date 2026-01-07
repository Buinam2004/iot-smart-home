package com.iot.controller;

import com.iot.dto.ResponseUserDTO;
import com.iot.dto.UserDTO;
import com.iot.entity.User;
import com.iot.exception.DuplicateResourceException;
import com.iot.exception.ResourceNotFoundException;
import com.iot.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    // GET /api/users - Lấy tất cả users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResponseUserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<ResponseUserDTO> responseUserDTOS = new ArrayList<>();
        for (User user : users) {
            ResponseUserDTO dto = new ResponseUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
            responseUserDTOS.add(dto);
        }
        return ResponseEntity.ok(responseUserDTOS);
    }
    
    // GET /api/users/{id} - Lấy user theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ResponseUserDTO> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        ResponseUserDTO dto = new ResponseUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(dto);
    }
    
    // POST /api/users - Tạo user mới
    @PostMapping
    public ResponseEntity<ResponseUserDTO> createUser(@RequestBody User user) {
        // Kiểm tra username và email đã tồn tại
        if (userService.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userService.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        User createdUser = userService.createUser(user);
        ResponseUserDTO dto = new ResponseUserDTO(createdUser.getId(), createdUser.getUsername(), createdUser.getEmail(), createdUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    // PATCH /api/users/{id} - Cập nhật user
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<ResponseUserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        ResponseUserDTO dto = new ResponseUserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(), updatedUser.getRole());
        return ResponseEntity.ok(dto);
    }
    
    // DELETE /api/users/{id} - Xóa user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
