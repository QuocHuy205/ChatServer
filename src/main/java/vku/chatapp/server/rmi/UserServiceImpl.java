package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.UserDTO;
import vku.chatapp.common.enums.UserStatus;
import vku.chatapp.common.rmi.IUserService;
import vku.chatapp.server.service.UserManagementService;
import vku.chatapp.server.service.CloudinaryService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UserServiceImpl extends UnicastRemoteObject implements IUserService {
    private final UserManagementService userService;
    private final CloudinaryService cloudinaryService;
    private static final String TEMP_AVATAR_DIR = "temp_avatars/";

    public UserServiceImpl() throws RemoteException {
        super();
        this.userService = new UserManagementService();
        this.cloudinaryService = CloudinaryService.getInstance();

        // Create temp directory
        try {
            Files.createDirectories(Paths.get(TEMP_AVATAR_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create temp directory: " + e.getMessage());
        }
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

    @Override
    public String uploadAvatar(Long userId, byte[] imageData, String fileName) throws RemoteException {
        Path tempFile = null;
        try {
            // 1. Save image data to temporary file
            String tempFileName = "temp_" + userId + "_" + System.currentTimeMillis() + "_" + fileName;
            tempFile = Paths.get(TEMP_AVATAR_DIR + tempFileName);
            Files.write(tempFile, imageData);

            System.out.println("📁 Temporary file created: " + tempFile);

            // 2. Upload to Cloudinary
            String cloudinaryUrl = cloudinaryService.uploadAvatar(
                    tempFile.toString(),
                    userId
            );

            if (cloudinaryUrl == null) {
                throw new RemoteException("Failed to upload to Cloudinary");
            }

            System.out.println("✅ Avatar uploaded to Cloudinary: " + cloudinaryUrl);
            return cloudinaryUrl;

        } catch (Exception e) {
            System.err.println("❌ Failed to upload avatar: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Failed to upload avatar", e);
        } finally {
            // 3. Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    System.out.println("🗑️ Temporary file deleted: " + tempFile);
                } catch (IOException e) {
                    System.err.println("Failed to delete temp file: " + e.getMessage());
                }
            }
        }
    }
}