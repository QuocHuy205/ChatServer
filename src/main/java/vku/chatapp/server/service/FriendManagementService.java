package vku.chatapp.server.service;

import vku.chatapp.common.dto.UserDTO;
import vku.chatapp.common.model.Friend;
import vku.chatapp.common.model.User;
import vku.chatapp.server.database.dao.FriendDAO;
import vku.chatapp.server.database.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class FriendManagementService {
    private final FriendDAO friendDAO;
    private final UserDAO userDAO;

    public FriendManagementService() {
        this.friendDAO = new FriendDAO();
        this.userDAO = new UserDAO();
    }

    public boolean sendFriendRequest(Long userId, Long friendId) {
        try {
            friendDAO.createFriendRequest(userId, friendId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptFriendRequest(Long requestId) {
        try {
            return friendDAO.acceptFriendRequest(requestId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rejectFriendRequest(Long requestId) {
        try {
            return friendDAO.rejectFriendRequest(requestId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFriend(Long userId, Long friendId) {
        try {
            return friendDAO.removeFriend(userId, friendId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserDTO> getFriendList(Long userId) {
        try {
            List<Long> friendIds = friendDAO.getFriendIds(userId);
            List<UserDTO> friends = new ArrayList<>();

            for (Long friendId : friendIds) {
                User user = userDAO.getUserById(friendId);
                if (user != null) {
                    friends.add(convertToDTO(user));
                }
            }

            return friends;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Friend> getPendingRequests(Long userId) {
        try {
            return friendDAO.getPendingRequests(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
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