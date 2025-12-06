package vku.chatapp.server.service;

import vku.chatapp.server.config.ConfigLoader;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String smtpHost;
    private final String smtpPort;
    private final String emailFrom;
    private final String emailPassword;

    public EmailService() {
        ConfigLoader config = ConfigLoader.getInstance();
        this.smtpHost = config.getProperty("email.smtp.host", "smtp.gmail.com");
        this.smtpPort = config.getProperty("email.smtp.port", "587");
        this.emailFrom = config.getProperty("email.from");
        this.emailPassword = config.getProperty("email.password");
    }

    /**
     * Gửi email xác thực đăng ký với OTP
     */
    public boolean sendVerificationEmail(String toEmail, String otp, String username) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã xác thực OTP - Đăng ký tài khoản Chat App");

            String emailContent = buildVerificationEmailTemplate(otp, username);
            message.setContent(emailContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✓ Email OTP đã được gửi đến: " + toEmail);

            // TEST MODE: In ra console để debug
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📧 EMAIL VERIFICATION SENT");
            System.out.println("To: " + toEmail);
            System.out.println("OTP: " + otp);
            System.out.println("Username: " + username);
            System.out.println("=".repeat(60) + "\n");

            return true;

        } catch (MessagingException e) {
            System.err.println("✗ Lỗi gửi email verification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gửi email đặt lại mật khẩu với OTP
     */
    public boolean sendPasswordResetEmail(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🔐 Mã xác thực đặt lại mật khẩu - Chat App");

            String emailContent = buildPasswordResetEmailTemplate(otp);
            message.setContent(emailContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✓ Email reset password đã được gửi đến: " + toEmail);

            // TEST MODE: In ra console để debug
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 EMAIL PASSWORD RESET SENT");
            System.out.println("To: " + toEmail);
            System.out.println("OTP: " + otp);
            System.out.println("=".repeat(60) + "\n");

            return true;

        } catch (MessagingException e) {
            System.err.println("✗ Lỗi gửi email reset password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Template HTML cho email xác thực đăng ký
     */
    private String buildVerificationEmailTemplate(String otp, String username) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác thực OTP</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden;">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">
                                            🔐 Xác Thực Tài Khoản
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <p style="color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                            Xin chào <strong>%s</strong>,
                                        </p>
                                        
                                        <p style="color: #555555; font-size: 15px; line-height: 1.6; margin: 0 0 30px 0;">
                                            Cảm ơn bạn đã đăng ký tài khoản tại <strong>Chat App</strong>. 
                                            Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã OTP bên dưới:
                                        </p>
                                        
                                        <!-- OTP Box -->
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 20px 0;">
                                                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                                                padding: 25px 50px; 
                                                                border-radius: 10px; 
                                                                display: inline-block;
                                                                box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);">
                                                        <span style="color: #ffffff; 
                                                                     font-size: 36px; 
                                                                     font-weight: bold; 
                                                                     letter-spacing: 8px;
                                                                     font-family: 'Courier New', monospace;">
                                                            %s
                                                        </span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Info Box -->
                                        <div style="background-color: #fff3cd; 
                                                    border-left: 4px solid #ffc107; 
                                                    padding: 15px 20px; 
                                                    border-radius: 5px; 
                                                    margin-top: 30px;">
                                            <p style="color: #856404; font-size: 14px; margin: 0; line-height: 1.6;">
                                                <strong>⚠️ Lưu ý:</strong><br>
                                                • Mã OTP có hiệu lực trong <strong>5 phút</strong><br>
                                                • Không chia sẻ mã này với bất kỳ ai<br>
                                                • Nếu không yêu cầu, vui lòng bỏ qua email này
                                            </p>
                                        </div>
                                        
                                        <p style="color: #666666; font-size: 14px; margin: 30px 0 0 0; line-height: 1.6;">
                                            Nếu bạn gặp vấn đề, vui lòng liên hệ với chúng tôi.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                        <p style="color: #6c757d; font-size: 13px; margin: 0 0 10px 0;">
                                            © 2025 Chat App. All rights reserved.
                                        </p>
                                        <p style="color: #adb5bd; font-size: 12px; margin: 0;">
                                            Email tự động, vui lòng không trả lời.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(username, otp);
    }

    /**
     * Template HTML đơn giản cho email đặt lại mật khẩu
     * (User sẽ nhập OTP và password mới trong app)
     */
    private String buildPasswordResetEmailTemplate(String otp) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Đặt lại mật khẩu</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fa;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 40px 0;">
                    <tr>
                        <td align="center">
                            <table width="500" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                                        <h2 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600;">
                                            🔐 Đặt Lại Mật Khẩu
                                        </h2>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 30px; text-align: center;">
                                        <p style="color: #333; margin: 0 0 15px 0; font-size: 15px; line-height: 1.6;">
                                            Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn.
                                        </p>
                                        
                                        <p style="color: #666; margin: 0 0 25px 0; font-size: 14px;">
                                            Vui lòng nhập mã OTP này vào ứng dụng:
                                        </p>
                                        
                                        <!-- OTP Box -->
                                        <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                                                    padding: 20px 40px; 
                                                    border-radius: 8px; 
                                                    margin: 25px auto;
                                                    display: inline-block;
                                                    box-shadow: 0 4px 15px rgba(240, 147, 251, 0.4);">
                                            <span style="color: #ffffff; 
                                                         font-size: 32px; 
                                                         font-weight: bold; 
                                                         letter-spacing: 5px;
                                                         font-family: 'Courier New', monospace;">
                                                %s
                                            </span>
                                        </div>
                                        
                                        <!-- Warning -->
                                        <div style="background-color: #fff3cd; 
                                                    border-left: 3px solid #ffc107; 
                                                    padding: 12px 15px; 
                                                    margin: 25px 0 0 0;
                                                    text-align: left;
                                                    border-radius: 4px;">
                                            <p style="color: #856404; font-size: 13px; margin: 0; line-height: 1.5;">
                                                <strong>⚠️ Lưu ý:</strong> Mã có hiệu lực trong <strong>5 phút</strong>. 
                                                Không chia sẻ mã này với ai.
                                            </p>
                                        </div>
                                        
                                        <p style="color: #999; font-size: 12px; margin: 20px 0 0 0; font-style: italic;">
                                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; border-top: 1px solid #e9ecef;">
                                        <p style="color: #6c757d; font-size: 12px; margin: 0;">
                                            © 2025 Chat App. Email tự động, vui lòng không trả lời.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(otp);
    }
}