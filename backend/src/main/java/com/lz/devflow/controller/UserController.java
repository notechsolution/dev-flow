package com.lz.devflow.controller;

import com.lz.devflow.dto.UserDTO;
import com.lz.devflow.service.UserService;
import jakarta.annotation.Resource;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/loggedInUser")
    public ResponseEntity<Object> getUser(@AuthenticationPrincipal OidcUser principal) {
        return ResponseEntity.ok(UserDTO.fromUser(userService.findCurrentLoginUser()));
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<Object> getUserList(@RequestParam("currentPage") int currentPage,
                                              @RequestParam("pageSize") int pageSize) {
        return ResponseEntity.ok(userService.getUserByPage(currentPage, pageSize));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<Object> inviteUser(@RequestBody UserDTO userDTO) throws OAuthProblemException, OAuthSystemException, ParseException {
//        userService.inviteUser(userDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAnyRole(T(com.lz.devflow.constant.UserRole).ADMIN.name(), T(com.lz.devflow.constant.UserRole).OPERATOR.name())")
    public ResponseEntity<Object> updateUser(@RequestBody UserDTO userDTO, @PathVariable String userId) {
        userService.updateUser(userId, userDTO);
        return ResponseEntity.ok().build();
    }
}
