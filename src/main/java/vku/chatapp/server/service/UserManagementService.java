package vku.chatapp.server.service;

import vku.chatapp.common.dto.UserDTO;
import vku.chatapp.common.enums.UserStatus;
import vku.chatapp.common.model.User;
import vku.chatapp.server.database.dao.UserDAO;

import java.util.List;
import java.util.stream.Collectors;

public class UserManagementService {
    private final UserDAO userDAO;

    public UserManagementService() {
        this.userDAO = new UserDAO();
    }

    public UserDTO getUserById(Long userId) {
        try {
            User user = userDAO.getUserById(userId);
            return user != null ? convertToDTO(user) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserDTO getUserByUsername(String username) {
        try {
            User user = userDAO.getUserByUsername(username);
            return user != null ? convertToDTO(user) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserDTO> searchUsers(String query) {
        try {
            List<User> users = userDAO.searchUsers(query);
            return users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean updateProfile(Long userId, String displayName, String bio, String avatarUrl) {
        try {
            return userDAO.updateProfile(userId, displayName, bio, avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(Long userId, UserStatus status) {
        try {
            return userDAO.updateStatus(userId, status);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setStatus(user.getStatus());
        return dto;
    }
}