package com.leviipope.todoapp.controller;

import com.leviipope.todoapp.security.JwtUtil;
import com.leviipope.todoapp.dto.LoginResponse;
import com.leviipope.todoapp.dto.UserCredentialsRequest;
import com.leviipope.todoapp.model.User;
import com.leviipope.todoapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public ProfileController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping
    public ResponseEntity<?> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserCredentialsRequest request) {
        try {
            User updatedUser = userService.updateCurrentUser(userDetails.getUsername(), request);

            // If the username was changed, generate a new token
            if (request.getUsername() != null && !request.getUsername().equals(userDetails.getUsername())) {
                String newToken = jwtUtil.generateToken(updatedUser);
                return ResponseEntity.ok(new LoginResponse(newToken, updatedUser.getUsername(), updatedUser.getRole()));
            }

            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.deleteCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok("Account deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
