/**
 * Email.java
 * Created by Damien Robinson
 * <p>
 * This class is used strictly to process emails that have been downloaded as text files.
 * It searches emails and stores the data it needs from them (Case ID, Date, IP and Email Body)
 * This data can then be used by the main function for finding users.
 */

package CopyrightProgram;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {

    private List<String> mailBody;
    private String caseID;
    private String Date;
    private String Timezone;
    private String IP;
    private Pattern dateRegex = Pattern.compile("([\\s\\S]*)(Timestamp:)(\\s*)" + //Preceding label (Group 1)
            "(((-?)([0-9]{2,4})){3})((\\s*)|(T))(((:?)([0-9]{2})){3})" + //Date and Time (Group 2)
            "((Z)|((\\s*)(\\D){27}))" +
            "([\\s\\S]*)");
    private Pattern caseIDRegex = Pattern.compile("(([\\s\\S]*)((Ref.)|((Case)(\\s*)(#:)))(\\s*))" + //Preceding Label (Group 1)
            "(((C)|([0-9]))([0-9]{8}))" + //Case ID format (Group 2)
            "([\\s\\S]*)");
    private Pattern ipRegex = Pattern.compile("((?i)([\\s\\S]*)(IP)(\\s*)(Address)(:?)(\\s*))" + //Preceding label (Group 1)
            "(([0-9]{3})((.)([0-9]{1,3})){3})" + //IP address to check for (Group 2)
            "([\\s\\S]*)");

    /**
     * Default constructor for the email class
     * Processes an email based on the path
     * @param pathToEmail
     */
    public Email(String pathToEmail) {
        mailBody = new ArrayList<>();
        processEmail(pathToEmail);
    }

    /**
     * Takes the file to process and checks line by line.
     * Searches for the date, caseID and IP and stores the email body
     * @param messageFile
     */
    private void processEmail(String messageFile) {

        Matcher dateMatcher, caseMatcher, ipMatcher;
        Boolean foundCase = false;
        Boolean foundDate = false;
        Boolean foundIP = false;
        try { //Try to open the message file
            BufferedReader in = new BufferedReader(new FileReader(messageFile));
            //Check each line of the message file
            String line;
            while ((line = in.readLine()) != null) {
                //Save each into a string
                mailBody.add(line + "\n");
                //Check if date has been found
                if (!foundDate) {
                    //Set the search to look for a date
                    dateMatcher = dateRegex.matcher(line);
                    //Check if the line contains a date
                    if (dateMatcher.find()) {
                        Date = (dateMatcher.group(4) + dateMatcher.group(8) + dateMatcher.group(11));
                        Timezone = dateMatcher.group(15);
                        //Stop looking for Dates
                        foundDate = true;
                    }
                }
                //Check if the case number has been found
                if (!foundCase) {
                    //Set the search to look for a Case ID
                    caseMatcher = caseIDRegex.matcher(line);
                    //Check if the line contains a Case ID
                    if (caseMatcher.find()) {
                        caseID = caseMatcher.group(10);
                        //Stop looking for Case IDs
                        foundCase = true;
                    }
                }
                //Check if the IP is found
                if (!foundIP) {
                    //Set the search to look for the IP
                    ipMatcher = ipRegex.matcher(line);
                    //Check if the line contains an IP
                    if (ipMatcher.find()) {
                        IP = ipMatcher.group(8);
                        //Stop looking for IPs
                        foundIP = true;
                    }
                }
            }
            in.close();
        } catch (IOException e) {
        }

    }

    /**
     * Returns the string value for the case ID
     * @return The Case ID
     */
    public String getID() {
        return caseID;
    }

    /**
     * Return the string value for the date
     * @return The date
     */
    public String getDate() {
        return Date;
    }

    /**
     * Return the string value of the Timezone
     * @return the Timezone
     */
    public String getTimezone() {
        return Timezone;
    }

    /**
     * Return the string value of the IP
     * @return The IP address
     */
    public String getIP() {
        return IP;
    }

    /**
     * Return a string containing the entire body of the email
     * @return The body of the email.
     */
    public String getMailBody() {
        StringBuilder builder = new StringBuilder();
        for (String value : mailBody) builder.append(value);
        return builder.toString();
    }
}
