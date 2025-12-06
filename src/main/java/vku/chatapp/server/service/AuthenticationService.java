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
            User user = userDAO.getUserByEmail(request.getEmail());
            boolean verifyByEmail = userDAO.getverifyEmail(request.getEmail());

            if (user == null) {
                return new AuthResponse(false, "Invalid username or password");
            }

            if (!EncryptionUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                return new AuthResponse(false, "Invalid username or password");
            }

            if (!verifyByEmail) {
                return new AuthResponse(false, "Email not verified. Please verify your email before logging in." + verifyByEmail);
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
        System.out.println("\n========== BẮT ĐẦU ĐĂNG KÝ ==========");
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();
        String displayName = request.getDisplayName();

        System.out.println("Username: " + username);
        System.out.println("Email: " + email);

        try {
            // 1. VALIDATE INPUT (Kiểm tra dữ liệu đầu vào chặt chẽ)
            if (username == null || username.trim().isEmpty()) {
                return new AuthResponse(false, "Tên đăng nhập không được để trống");
            }
            // Regex kiểm tra email chuẩn
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return new AuthResponse(false, "Email không hợp lệ");
            }
            if (password == null || password.length() < 6) {
                return new AuthResponse(false, "Mật khẩu phải có ít nhất 6 ký tự");
            }

            // 2. CHECK EXISTING DATA (Kiểm tra tồn tại trong DB)
//            if (userDAO.getUserByUsername(username) != null) {
//                return new AuthResponse(false, "Tên đăng nhập đã tồn tại");
//            }
            if (userDAO.getUserByEmail(email) != null) {
                return new AuthResponse(false, "Email đã được sử dụng bởi tài khoản khác");
            }

            // 3. CREATE USER (Tạo đối tượng User)
            String passwordHash = EncryptionUtil.hashPassword(password); // Dùng EncryptionUtil của bạn

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordHash);
            newUser.setDisplayName(displayName != null ? displayName : username);
            newUser.setEmailVerified(false); // Quan trọng: Mặc định là chưa xác thực

            // Lưu User vào DB
            // Lưu ý: Nếu userDAO của bạn tự quản lý connection thì dòng này sẽ commit luôn
            newUser = userDAO.createUser(newUser);

            if (newUser == null || newUser.getId() <= 0) {
                return new AuthResponse(false, "Lỗi hệ thống: Không thể tạo tài khoản vào Database");
            }
            String otpCode = otpDAO.generateOtp(email, "EMAIL_VERIFICATION");

            if (otpCode == null) {
                // Nếu lỗi tạo OTP, có thể cần xóa user vừa tạo để tránh rác (tùy chọn)
                // userDAO.deleteUser(newUser.getId());
                return new AuthResponse(false, "Lỗi hệ thống: Không thể tạo mã OTP");
            }

            // 5. SEND EMAIL (Gửi mail)
            try {
                if (!emailService.sendVerificationEmail(email, otpCode, username))
                    throw new Exception("Gửi email thất bại");

                System.out.println("Đã gửi email OTP đến: " + email);
            } catch (Exception e) {
                e.printStackTrace();
                // Logic Rollback thủ công: Nếu gửi mail lỗi thì nên xóa User đi để họ đăng ký lại được
                // userDAO.deleteUser(newUser.getId());
                return new AuthResponse(false, "Không thể gửi email xác thực. Vui lòng kiểm tra lại địa chỉ email.");
            }

            // 6. RETURN SUCCESS
            AuthResponse response = new AuthResponse(true, "Đăng ký thành công. Vui lòng kiểm tra email để nhập mã OTP.");
            response.setUser(newUser);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Lỗi xử lý đăng ký: " + e.getMessage());
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