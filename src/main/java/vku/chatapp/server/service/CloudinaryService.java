package vku.chatapp.server.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.util.Map;

public class CloudinaryService {
    private static CloudinaryService instance;
    private final Cloudinary cloudinary;

    // TODO: Replace with your Cloudinary credentials from dashboard
    // Get them from: https://console.cloudinary.com/
    private static final String CLOUD_NAME = "ddierfipn";  // e.g., "dxxxx123"
    private static final String API_KEY = "163745366611248";        // e.g., "123456789012345"
    private static final String API_SECRET = "HtENGUZDFf2mqYTjRghb8zCL6Mo";  // e.g., "abcdef..."

    private CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET
        ));
        System.out.println("✅ Cloudinary Service initialized");
    }

    public static synchronized CloudinaryService getInstance() {
        if (instance == null) {
            instance = new CloudinaryService();
        }
        return instance;
    }

    /**
     * Upload avatar to Cloudinary
     * @param localFilePath Path to local file
     * @param userId User ID (used for folder organization)
     * @return URL of uploaded image
     */
    public String uploadAvatar(String localFilePath, Long userId) {
        try {
            File file = new File(localFilePath);

            // Upload with options (simpler version without transformation)
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "chatapp/avatars/user_" + userId,
                    "public_id", "avatar_" + System.currentTimeMillis(),
                    "overwrite", true,
                    "resource_type", "image"
            ));

            String url = (String) uploadResult.get("secure_url");
            System.out.println("✅ Avatar uploaded to Cloudinary: " + url);
            return url;

        } catch (Exception e) {
            System.err.println("❌ Failed to upload to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete avatar from Cloudinary
     * @param imageUrl Full URL of the image
     */
    public boolean deleteAvatar(String imageUrl) {
        try {
            // Extract public_id from URL
            // Example URL: https://res.cloudinary.com/CLOUD_NAME/image/upload/v123/folder/public_id.jpg
            String publicId = extractPublicId(imageUrl);

            if (publicId == null) {
                return false;
            }

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("🗑️ Avatar deleted from Cloudinary: " + publicId);
            return "ok".equals(result.get("result"));

        } catch (Exception e) {
            System.err.println("❌ Failed to delete from Cloudinary: " + e.getMessage());
            return false;
        }
    }

    private String extractPublicId(String url) {
        // Extract public_id from Cloudinary URL
        // Example: https://res.cloudinary.com/demo/image/upload/v123/folder/avatar_123.jpg
        // Public ID: folder/avatar_123

        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;

            String afterUpload = parts[1];
            // Remove version (v123456/) if exists
            if (afterUpload.startsWith("v")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // Remove file extension
            return afterUpload.substring(0, afterUpload.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }
}