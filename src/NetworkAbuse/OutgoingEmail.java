package NetworkAbuse;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by Damien Robinson on 10/06/2016.
 */
public class OutgoingEmail {
    private static String email, body, user, name, subject, ip, mac, date, caseID, fullName;
    public String details;
    private CredentialManager credentialManager;

    public OutgoingEmail(CredentialManager credentialManager, String emailSubject, String emailBody) {
        this.credentialManager = credentialManager;
        subject = emailSubject;
        body = emailBody;
    }

    public void setUserDetails(String username, String givenname, String fullName, String emailAddress, String abuseIP,
                               String macAddr, String dateTime, String ID) throws IOException {
        email = emailAddress;
        user = username;
        name = givenname;
        ip = abuseIP;
        mac = macAddr;
        date = dateTime;
        caseID = ID;
        this.fullName = fullName;
        details = "\nUsername: " + user + "\nName: " + this.fullName + "\nEmail Address: " + email + "\nIP: " + ip +
                "\nMAC Address: " + mac + "\nDate & Time: " + date + "\nCase ID: " + caseID + "\n";

        String write = user + "," + fullName + "," + email + "," + ip + "," + mac + "," + date + "," + caseID;
        FileWriter fw = new FileWriter(".\\NetworkAbusers.csv", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter writer = new PrintWriter(bw);
        writer.println(write);
        writer.close();

    }

    public void notifyClient() throws IOException {
        String emailBody =
                "Hello " + name + "," +
                        "\n\n" +
                        "This email serves as a notice that your Username: " + user +
                        " was using the specified IP address: " + ip +
                        " on the following device with MAC Address: " + mac +
                        " during the time of the claimed infringement listed in the email below. Saint Mary's University is not " +
                        "affiliated with the company claiming the infringement, but we have a legal obligation to forward their" +
                        " notice to you." +
                        "\n\n" +
                        "Regards,\n" +
                        "Information Technology Systems & Support,\n" +
                        "Saint Mary's University\n\n" +
                        "Please note that you are responsible for any device that you connect to Saint Mary's network.\n" +
                        "If you do not own the device above, please change your password as soon as possible.\n\n" +
                        "***NOTE: Do not reply to this email.\n\n" + body;
        SendEmail(emailBody, subject, email);
    }

    public void makeTicket() throws IOException {
        SendEmail(details, "Copyright Infringement Notice", "tdcopyright@smu.ca");
    }

    private void SendEmail(String emailBody, String emailSubject, String to) throws IOException {
        // Sender's email ID needs to be mentioned
        String from = credentialManager.getUsername();

        // Get system properties
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.smu.ca");
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");

        Session session1 = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(from, credentialManager.getPassword());
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

        } catch (SendFailedException invalid) {
            invalid.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
