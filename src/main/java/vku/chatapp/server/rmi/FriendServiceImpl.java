package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.UserDTO;
import vku.chatapp.common.model.Friend;
import vku.chatapp.common.rmi.IFriendService;
import vku.chatapp.server.service.FriendManagementService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class FriendServiceImpl extends UnicastRemoteObject implements IFriendService {
    private final FriendManagementService friendService;

    public FriendServiceImpl() throws RemoteException {
        super();    
        this.friendService = new FriendManagementService();
    }

    @Override
    public boolean sendFriendRequest(Long userId, Long friendId) throws RemoteException {
        try {
            return friendService.sendFriendRequest(userId, friendId);
        } catch (Exception e) {
            throw new RemoteException("Failed to send friend request", e);
        }
    }

    @Override
    public boolean acceptFriendRequest(Long requestId) throws RemoteException {
        try {
            return friendService.acceptFriendRequest(requestId);
        } catch (Exception e) {
            throw new RemoteException("Failed to accept friend request", e);
        }
    }

    @Override
    public boolean rejectFriendRequest(Long requestId) throws RemoteException {
        try {
            return friendService.rejectFriendRequest(requestId);
        } catch (Exception e) {
            throw new RemoteException("Failed to reject friend request", e);
        }
    }

    @Override
    public boolean cancelFriendRequest(Long requestId, Long userId) throws RemoteException {
        try {
            return friendService.cancelFriendRequest(requestId, userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to cancel friend request", e);
        }
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) throws RemoteException {
        try {
            return friendService.removeFriend(userId, friendId);
        } catch (Exception e) {
            throw new RemoteException("Failed to remove friend", e);
        }
    }

    @Override
    public List<UserDTO> getFriendList(Long userId) throws RemoteException {
        try {
            return friendService.getFriendList(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get friend list", e);
        }
    }

    @Override
    public List<Friend> getPendingRequests(Long userId) throws RemoteException {
        try {
            return friendService.getPendingRequests(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get pending requests", e);
        }
    }

    @Override
    public List<Friend> getSentRequests(Long userId) throws RemoteException {
        try {
            return friendService.getSentRequests(userId);
        } catch (Exception e) {
            throw new RemoteException("Failed to get sent requests", e);
        }
    }

    @Override
    public UserDTO searchUserByUsername(String username) throws RemoteException {
        try {
            return friendService.searchUserByUsername(username);
        } catch (Exception e) {
            throw new RemoteException("Failed to search user", e);
        }
    }
}