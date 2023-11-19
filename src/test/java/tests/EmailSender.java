package tests;

import com.google.api.client.util.DateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {
    public static void sendEmail(String bodyEmailMessage, String emailSubject) throws Exception {
        ReadConfig readConfig = new ReadConfig();
        Map<String, Object> config = readConfig.parseJsonToObject();

        final String username = getConfigValue(config, "username");
        final String password = getPasswordConfigValue(config, "password");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("soul3510@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("soul3510@gmail.com"));
            message.setSubject("SYNC-GAMES-V4 finished running");

            LocalDateTime currentDateTime = LocalDateTime.now();

            // Format the current date and time (optional)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);

            message.setText("SYNC-GAMES-V4 finished running at: " + formattedDateTime);
            message.setText(bodyEmailMessage);

            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigValue(Map<String, Object> config, String key) {
        return config.get(key).toString();
    }

    private static String getPasswordConfigValue(Map<String, Object> config, String key) {
        String password = config.get(key).toString();
        return PasswordEncryption.decrypt(password);
    }
}
