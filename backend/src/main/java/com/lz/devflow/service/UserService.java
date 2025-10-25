package com.lz.devflow.service;

import com.lz.devflow.constant.UserRole;
import com.lz.devflow.dto.UserDTO;
import com.lz.devflow.entity.User;
import com.lz.devflow.exception.BadRequestException;
import com.lz.devflow.exception.UserNotFoundException;
import com.lz.devflow.repository.UserRepository;
import com.lz.devflow.util.SecurityUtils;
import jakarta.annotation.Resource;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserRepository userRepository;


//    public User findUserByOid(String oid) {
//        return userRepository.findByOid(oid);
//    }

    public User findCurrentLoginUser() {
//        String oid = SecurityUtils.getCurrentUserOid();
//        if (Objects.nonNull(oid)) {
//            return userRepository.findByOid(oid);
//        }
        return null;
    }

    public Page<User> getUserByPage(int currentPage, int pageSize) {
        Pageable pageable = Pageable.ofSize(pageSize).withPage(currentPage);
        User user = findCurrentLoginUser();
        Page<User> page;
        if (user.getRole().equals(UserRole.ADMIN)) {
            page = userRepository.findByProjectIdsInAndRoleIsNot(user.getProjectIds(), UserRole.OPERATOR.name(), pageable);
            page.getContent().forEach(u -> u.setProjectIds(u.getProjectIds().stream().filter(user.getProjectIds()::contains).toList()));
        } else {
            page = userRepository.findAll(pageable);
        }
        return page;
    }

//    public void inviteUser(UserDTO userDTO) throws OAuthProblemException, OAuthSystemException, ParseException {
//        User currentUser = findCurrentLoginUser();
//        User user = userDTO.toUser();
//        if (currentUser.getRole().equals(UserRole.ADMIN) && !new HashSet<>(currentUser.getProjectIds()).containsAll(user.getProjectIds())) {
//            throw new BadRequestException();
//        }
//        User dbUser = userRepository.findByOid(user.getOid());
//        if (Objects.isNull(dbUser)) {
//            user.setCreatedBy(currentUser.getUsername());
//        } else {
//            if (currentUser.getRole().equals(UserRole.ADMIN)) {
//                Set<String> removeSet = new HashSet<>(currentUser.getProjectIds());
//                user.getProjectIds().forEach(removeSet::remove);
//                user.setProjectIds(dbUser.getProjectIds());
//                user.getProjectIds().addAll(currentUser.getProjectIds());
//                user.setProjectIds(user.getProjectIds().stream().distinct().collect(Collectors.toList()));
//                user.getProjectIds().removeAll(removeSet);
//            } else {
//                user.setProjectIds(dbUser.getProjectIds());
//            }
//            user.setCreatedBy(dbUser.getCreatedBy());
//        }
//        user.setUpdatedBy(currentUser.getUsername());
//        userRepository.save(user);
//    }

    public void updateUser(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId);
        User currentUser = findCurrentLoginUser();
        if (Objects.nonNull(user)) {
            if (currentUser.getRole().equals(UserRole.ADMIN)) {
                if (user.getProjectIds().stream().noneMatch(currentUser.getProjectIds()::contains) || user.getRole().equals(UserRole.OPERATOR)) {
                    throw new UserNotFoundException();
                }
            }
            user.setRole(userDTO.getRole());
            if (currentUser.getRole().equals(UserRole.ADMIN)) {
                Set<String> removeSet = new HashSet<>(currentUser.getProjectIds());
                userDTO.getProjectIds().forEach(removeSet::remove);
                userDTO.setProjectIds(user.getProjectIds());
                userDTO.getProjectIds().addAll(currentUser.getProjectIds());
                userDTO.setProjectIds(userDTO.getProjectIds().stream().distinct().collect(Collectors.toList()));
                userDTO.getProjectIds().removeAll(removeSet);
            }
            if (user.getRole().equals(UserRole.OPERATOR)) {
                user.setProjectIds(new ArrayList<>());
            } else {
                user.setProjectIds(userDTO.getProjectIds());
            }
            user.setUpdatedBy(currentUser.getUsername());
            userRepository.save(user);
        } else {
            throw new UserNotFoundException();
        }
    }
}
