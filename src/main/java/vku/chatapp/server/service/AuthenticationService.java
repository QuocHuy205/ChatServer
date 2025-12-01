package vku.chatapp.server.service;

import vku.chatapp.common.dto.*;
import vku.chatapp.common.model.User;
import vku.chatapp.common.util.EncryptionUtil;
import vku.chatapp.server.database.dao.UserDAO;
import vku.chatapp.server.database.dao.OtpDAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationService {
    private final UserDAO userDAO;
    private final OtpDAO otpDAO;
    private final EmailService emailService;
    private final Map<String, Long> activeSessions;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.otpDAO = new OtpDAO();
        this.emailService = new EmailService();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            User user = userDAO.getUserByUsername(request.getUsername());

            if (user == null) {
                return new AuthResponse(false, "Invalid username or password");
            }

            if (!EncryptionUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                return new AuthResponse(false, "Invalid username or password");
            }

            // Removed email verification check - allow login without verification

            String sessionToken = EncryptionUtil.generateToken();
            activeSessions.put(sessionToken, user.getId());

            AuthResponse response = new AuthResponse(true, "Login successful");
            response.setUser(user);
            response.setSessionToken(sessionToken);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            // Check if username exists
            if (userDAO.getUserByUsername(request.getUsername()) != null) {
                return new AuthResponse(false, "Username already exists");
            }

            // Check if email exists
            if (userDAO.getUserByEmail(request.getEmail()) != null) {
                return new AuthResponse(false, "Email already registered");
            }

            // Create new user - auto verify email
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(EncryptionUtil.hashPassword(request.getPassword()));
            user.setDisplayName(request.getDisplayName());
            user.setEmailVerified(true); // Auto verify for easier testing

            user = userDAO.createUser(user);

            // No need to send verification email anymore
            // emailService.sendVerificationEmail(request.getEmail(), otp);

            AuthResponse response = new AuthResponse(true, "Registration successful. You can now login.");
            response.setUser(user);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }

    public boolean logout(String sessionToken) {
        return activeSessions.remove(sessionToken) != null;
    }

    public boolean verifyEmail(String email, String otp) {
        try {
            if (otpDAO.verifyOtp(email, otp, "EMAIL_VERIFICATION")) {
                User user = userDAO.getUserByEmail(email);
                if (user != null) {
                    return userDAO.verifyEmail(user.getId());
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendPasswordResetOtp(String email) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return false;
            }

            String otp = otpDAO.generateOtp(email, "PASSWORD_RESET");
            emailService.sendPasswordResetEmail(email, otp);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String email, String otp, String newPassword) {
        try {
            if (otpDAO.verifyOtp(email, otp, "PASSWORD_RESET")) {
                User user = userDAO.getUserByEmail(email);
                if (user != null) {
                    String passwordHash = EncryptionUtil.hashPassword(newPassword);
                    return userDAO.updatePassword(user.getId(), passwordHash);
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateSession(String sessionToken) {
        return activeSessions.containsKey(sessionToken);
    }
}