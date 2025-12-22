package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    // CẢNH BÁO: Sau khi test xong, bạn nên tạo App Password mới và xóa cái cũ này đi vì đã bị lộ.
    private static final String SENDER_EMAIL = "congthanh10000@gmail.com"; 
    private static final String SENDER_PASSWORD = "yxkkwmktzosgcnqw"; 

    public static String generateOTP() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public static void sendOTP(String recipientEmail, String otpCode) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("UNO GAMES - Password Reset OTP");
        
        // Sử dụng nội dung HTML đã build
        message.setContent(buildHtmlEmail(otpCode), "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("OTP Email sent successfully to " + recipientEmail);
    }

    private static String buildHtmlEmail(String otpCode) {
        // Dùng %s để thay thế otpCode vào vị trí mong muốn
        return String.format("""
        <!DOCTYPE html>
		<html lang="en">
		<head>
		    <meta charset="UTF-8">
		    <meta name="viewport" content="width=device-width, initial-scale=1.0">
		    <title>UNO GAMES</title>
		    <style>
		        /* Reset cơ bản */
		        body, html {
		            margin: 0;
		            padding: 0;
		            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
		            background-color: #0f0f0f;
		            color: #ffffff;
		        }
		
		        /* Container chính */
		        .container {
		            max-width: 600px;
		            margin: 40px auto;
		            background-color: #1c1c1c;
		            border-radius: 12px;
		            padding: 30px 25px;
		            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
		        }
		
		        /* Header */
		        .header h1 {
		            margin: 0;
		            font-size: 32px;
		            color: #e63946;
		            text-align: center;
		        }
		
		        /* Nội dung */
		        .content {
		            margin-top: 25px;
		            font-size: 16px;
		            line-height: 1.6;
		            color: white;
		        }
		
		        /* OTP code */
		        .otp {
		            display: block;
		            margin: 25px auto;
		            padding: 20px;
		            text-align: center;
		            font-size: 28px;
		            font-weight: bold;
		            color: #0f0f0f;
		            background: #e63946;
		            border-radius: 12px;
		            width: 180px;
		            letter-spacing: 4px;
		            box-shadow: 0 4px 10px rgba(230, 57, 70, 0.6);
		        }
		
		        /* Footer */
		        .footer {
		            text-align: center;
		            font-size: 13px;
		            color: #aaa;
		            margin-top: 30px;
		            line-height: 1.5;
		        }
		
		        /* Responsive */
		        @media (max-width: 640px) {
		            .container {
		                margin: 20px;
		                padding: 20px;
		            }
		            .otp {
		                width: 150px;
		                font-size: 24px;
		                padding: 15px;
		            }
		        }
		    </style>
		</head>
		<body>
		    <div class="container">
		        <div class="header">
		            <h1>UNO GAMES</h1>
		        </div>
		
		        <div class="content">
		            <p>Hello Gamer,</p>
		            <p>We received a request to access your UNO Games account. Use the verification code below to proceed:</p>
		
		            <div class="otp">%s</div>
		
		            <p>Please do not share this code with anyone. It will expire in 10 minutes.</p>
		
		            <p>If you did not request this code, you can safely ignore this email.</p>
		        </div>
		
		        <div class="footer">
		            &copy; 2025 UNO Games. All rights reserved.<br>
		            For support, contact <a href="mailto:support@unogames.com" style="color: #e63946; text-decoration: none;">support@unogames.com</a>
		        </div>
		    </div>
		</body>
		</html>

        """, otpCode);
    }
}