package NetworkAbuse;


import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static javax.mail.Folder.READ_WRITE;

/**
 * Created by Damien Robinson
 */
public class IncomingEmail {
    private List<File> clientLogs;
    private List<String> emailBody, emailSubject;
    private FileManager fileManager = new FileManager();
    private JLabel label;
    private Folder copyrightFolder, processedFolder, netlogFolder;

    public IncomingEmail(Store store, JLabel status) {

        try {
            label = status;

            clientLogs = new ArrayList<>();
            emailBody = new ArrayList<>();
            emailSubject = new ArrayList<>();

            if (!store.isConnected())
                store.connect();

            copyrightFolder = store.getFolder("NEW COPYRIGHTS");
            processedFolder = store.getFolder("OLD COPYRIGHTS");
            netlogFolder = store.getFolder("NETWORK LOGS");

            if (!copyrightFolder.isOpen())
                copyrightFolder.open(READ_WRITE);
            if (!netlogFolder.isOpen())
                netlogFolder.open(READ_WRITE);

            //if (netlogFolder.getMessageCount() > 0) {
            label.setText("Network log folder contains " + netlogFolder.getMessageCount() + " network logs");
            saveNetworkLogs();
            label.setText("Checking for new messages...");
            //}
            //if (copyrightFolder.getMessageCount() > 0) {
            //Copy messages to another folder for backup
            label.setText("Checking copyright messages...");
            //Save the emails
            saveCopyrightEmails();
            //Break out of the loop to begin processing any new emails.

            //}

            //Get a list of all the network logs we have saved.
            if (clientLogs.size() == 0) {
                for (File file : fileManager.fileList(".\\tmp\\tmp\\")) {
                    if (file.getName().contains("Client"))
                        clientLogs.add(file);
                }
            }
            store.close();
            return;
        } catch (Exception e){
            Main.textArea.append(e.toString());
        }
    }


    /**
     * The getTextFromMessage method extracts only plain text from the body of the
     * message item passed into it's parameters.
     *
     * @param message The message item to be processed
     * @return a String of text that is contained in the body of the message
     * @throws Exception
     */
    private String getTextFromMessage(Message message) throws Exception {
        //If the body only consists of plain text
        if (message.isMimeType("text/plain")) {
            //Return the body as a string
            return message.getContent().toString();
            //If the body contains items other than text.
        } else if (message.isMimeType("multipart/*")) {
            String result = "";
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            int count = mimeMultipart.getCount();
            //For each item in the body, get the string equivalent
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result = result + "\n" + bodyPart.getContent();
                    break;  //without break same text appears twice in my tests
                } else if (bodyPart.isMimeType("text/html")) {
                    //String html = (String) bodyPart.getContent();
                    //result = result + "\n" + Jsoup.parse(html).text();
                }
            }
            return result;
        }
        return "";
    }

    /**
     * The saveNetworkLogs method is used to get each message from the network logs folder
     * Get only the attachments from each of these messages, save them to a "tmp" folder
     * Unzip the attachments and save them to a "./tmp/tmp" folder
     *
     * @return the date of the newest network log.
     * @throws Exception
     */
    public void saveNetworkLogs() {
        try {
            Message[] netlogs = netlogFolder.getMessages();
            File folder = new File(".\\tmp\\");
            if (!folder.exists()) {
                folder.mkdir();
            }
            for (Message message : netlogs) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                            !StringUtils.isNotBlank(bodyPart.getFileName())) {
                        continue; // dealing with attachments only
                    }

                    label.setText("Saving: " + bodyPart.getFileName() + "...");
                    InputStream is = bodyPart.getInputStream();

                    File f = new File(folder + bodyPart.getFileName());
                    FileOutputStream fos = new FileOutputStream(f);
                    byte[] buf = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buf)) != -1) {
                        fos.write(buf, 0, bytesRead);
                    }
                    fos.close();
                    label.setText(bodyPart.getFileName() + " saved successfully.");
                    fileManager.unZipIt(f, ".\\tmp\\tmp\\");
                    label.setText("Unzipped: " + bodyPart.getFileName() + "...");

                }

                message.setFlag(Flags.Flag.DELETED, true);
            }
            label.setText("Deleting proccessed network logs...");
            netlogFolder.expunge();
            netlogFolder.close(false);
        } catch (Exception e){
            Main.textArea.append(e.toString());;
        }
    }

    /**
     * The saveCopyrightEmails method gets a list of all messages in the New Copyrights folder
     * for each message in the folder, this method adds the subject and the body to a list for
     * processing
     *
     * @throws Exception
     */
    public void saveCopyrightEmails() throws Exception {
        Message[] messages = copyrightFolder.getMessages();
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, -1);
        List<Message> processed = new ArrayList<>();
        Date yesterday = calendar.getTime();
        for (Message message : messages) {
            if (message.getReceivedDate().before(yesterday)) {
                try {
                    emailBody.add(getTextFromMessage(message));
                    emailSubject.add(message.getSubject());
                    label.setText("Saving messages... " + message.getSubject());
                    processed.add(message);
                    message.setFlag(Flags.Flag.DELETED, true);
                } catch (NullPointerException n) {
                }
            }
        }
        copyrightFolder.copyMessages(processed.toArray(new Message[processed.size()]), processedFolder);
        copyrightFolder.expunge();
        if (processed.size() > 0)
            label.setText("Messages have been saved successfully and moved to the old copyrights folder.");
        else
            label.setText("No new messages");

        copyrightFolder.close(false);
        return;
    }

    public String[] getEmailBody() {
        return emailBody.toArray(new String[emailBody.size()]);
    }

    public String[] getEmailSubject() {
        return emailSubject.toArray(new String[emailSubject.size()]);
    }

    public File[] getClientLogs() throws FileNotFoundException {
        return clientLogs.toArray(new File[clientLogs.size()]);
    }

}
