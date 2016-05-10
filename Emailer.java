package CopyrightProgram;

/**
 * Emailer.java
 * Created by Damien Robinson
 * <p>
 * This class handles all outgoing emails to notify clients and generate tickets
 */

// File Name SendEmail.java

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class Emailer {

    private static String email, body, user, name, subject, ip;

    public Emailer(String emailAddress, String emailBody, String username,
                   String clientName, String emailSubject, String abuseIP) {
        email = emailAddress;
        body = emailBody;
        user = username;
        name = clientName;
        ip = abuseIP;
        subject = emailSubject.replace(".txt", "");
    }

    public void notifyClient() {
        String emailBody =
                "Hello " + name + "," +
                        "\n\n" +
                        "This email serves as a notice that your username ( " + user + " ) was using the specified IP address ( " + ip + " ) during the time " +
                        "of the claimed infringement listed in the email below. Saint Mary's University is not " +
                        "affiliated with the company claiming the infringement, but we have a legal obligation to forward their" +
                        " notice to you." +
                        "\n\n" +
                        "Regards,\n" +
                        "Information Technology Systems & Support,\n" +
                        "Saint Mary's University\n\n" + body;
        SendEmail(emailBody, subject);
    }

    /*public void createTicket() {
    }*/


    private void SendEmail(String emailBody, String emailSubject) {
        // Recipient's email ID needs to be mentioned.
        PrivateStaticVariables privateVar= new PrivateStaticVariables();
        String to = email;
        //String to = email;
        String pwd = privateVar.getEmailPassword();
        // Sender's email ID needs to be mentioned
        String from = privateVar.getEmailAddress();

        // Get system properties
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.smu.ca");
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");

        Session session1 = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication("hd06", pwd);
                    }
                });

        try {

            Message message = new MimeMessage(session1);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(emailSubject);
            message.setText(emailBody);

            Transport.send(message);

            System.out.println(name);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}