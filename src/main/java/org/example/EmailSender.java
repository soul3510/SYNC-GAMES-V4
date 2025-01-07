package org.example;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

public class EmailSender {
    public static void sendEmail(String bodyEmailMessage, String emailSubject) throws Exception {

        String username = System.getenv("username");
        String password = System.getenv("password");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("soul3510@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("soul3510@gmail.com"));
            message.setSubject(emailSubject);

            LocalDateTime currentDateTime = LocalDateTime.now();

            // Format the current date and time (optional)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);

            message.setText("SYNC-GAMES-V4 finished running at: " + formattedDateTime + ", " + bodyEmailMessage);

            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
