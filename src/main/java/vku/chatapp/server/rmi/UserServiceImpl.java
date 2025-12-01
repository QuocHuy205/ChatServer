package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.UserDTO;
import vku.chatapp.common.enums.UserStatus;
import vku.chatapp.common.rmi.IUserService;
import vku.chatapp.server.service.UserManagementService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UserServiceImpl extends UnicastRemoteObject implements IUserService {
    private final UserManagementService userService;

    public UserServiceImpl() throws RemoteException {
        super();
        this.userService = new UserManagementService();
    }

    @Override
    public UserDTO getUserById(Long userId) throws RemoteException {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get user", e);
        }
    }

    @Override
    public UserDTO getUserByUsername(String username) throws RemoteException {
        try {
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            throw new RemoteException("Failed to get user", e);
        }
    }

    @Override
    public List<UserDTO> searchUsers(String query) throws RemoteException {
        try {
            return userService.searchUsers(query);
        } catch (Exception e) {
            throw new RemoteException("Failed to search users", e);
        }
    }

    @Override
    public boolean updateProfile(Long userId, String displayName, String bio, String avatarUrl) throws RemoteException {
        try {
            return userService.updateProfile(userId, displayName, bio, avatarUrl);
        } catch (Exception e) {
            throw new RemoteException("Failed to update profile", e);
        }
    }

    @Override
    public boolean updateStatus(Long userId, UserStatus status) throws RemoteException {
        try {
            return userService.updateStatus(userId, status);
        } catch (Exception e) {
            throw new RemoteException("Failed to update status", e);
        }
    }
}