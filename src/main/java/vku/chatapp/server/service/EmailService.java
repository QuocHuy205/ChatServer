package vku.chatapp.server.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String fromEmail = "noreply@vkuchat.com";
    private final String smtpHost = "smtp.gmail.com";
    private final String smtpPort = "587";

    public void sendVerificationEmail(String toEmail, String otp) {
        String subject = "VKU Chat - Email Verification";
        String body = String.format("""
            Hello,
            
            Thank you for registering with VKU Chat!
            
            Your verification code is: %s
            
            This code will expire in 5 minutes.
            
            If you didn't request this code, please ignore this email.
            
            Best regards,
            VKU Chat Team
            """, otp);

        sendEmail(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String otp) {
        String subject = "VKU Chat - Password Reset";
        String body = String.format("""
            Hello,
            
            You requested to reset your password.
            
            Your reset code is: %s
            
            This code will expire in 5 minutes.
            
            If you didn't request this reset, please ignore this email.
            
            Best regards,
            VKU Chat Team
            """, otp);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        // This is a skeleton implementation
        // In production, you would configure actual SMTP settings
        System.out.println("=== EMAIL SENT ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("==================");

        // Actual implementation would use JavaMail API:
        /*
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        */
    }
}