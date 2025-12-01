package vku.chatapp.server.rmi;

import vku.chatapp.common.dto.*;
import vku.chatapp.common.rmi.IAuthService;
import vku.chatapp.server.service.AuthenticationService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthServiceImpl extends UnicastRemoteObject implements IAuthService {
    private final AuthenticationService authService;

    public AuthServiceImpl() throws RemoteException {
        super();
        this.authService = new AuthenticationService();
    }

    @Override
    public AuthResponse login(LoginRequest request) throws RemoteException {
        try {
            return authService.login(request);
        } catch (Exception e) {
            throw new RemoteException("Login failed", e);
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) throws RemoteException {
        try {
            return authService.register(request);
        } catch (Exception e) {
            throw new RemoteException("Registration failed", e);
        }
    }

    @Override
    public boolean logout(String sessionToken) throws RemoteException {
        try {
            return authService.logout(sessionToken);
        } catch (Exception e) {
            throw new RemoteException("Logout failed", e);
        }
    }

    @Override
    public boolean verifyEmail(String email, String otp) throws RemoteException {
        try {
            return authService.verifyEmail(email, otp);
        } catch (Exception e) {
            throw new RemoteException("Email verification failed", e);
        }
    }

    @Override
    public boolean sendPasswordResetOtp(String email) throws RemoteException {
        try {
            return authService.sendPasswordResetOtp(email);
        } catch (Exception e) {
            throw new RemoteException("Failed to send OTP", e);
        }
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) throws RemoteException {
        try {
            return authService.resetPassword(email, otp, newPassword);
        } catch (Exception e) {
            throw new RemoteException("Password reset failed", e);
        }
    }

    @Override
    public boolean validateSession(String sessionToken) throws RemoteException {
        try {
            return authService.validateSession(sessionToken);
        } catch (Exception e) {
            throw new RemoteException("Session validation failed", e);
        }
    }
}