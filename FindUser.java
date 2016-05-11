/**
 * FindUser.java
 * Created by Damien Robinson
 * <p>
 * This Class contains the main function, it's purpose is to initialize instances
 * of all other class. This class does the searching for values in the CSV files given
 * to it from the emails.
 */

package CopyrightProgram;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class FindUser {

    private static String emailDirectory = ".\\CopyrightProgram\\emailmessages";

    public static void main(String[] args) throws NamingException {

        PrivateStaticVariables privateVar = new PrivateStaticVariables();
        //Create instance of active directory session
        ActiveDirectory activeDirectory = new ActiveDirectory(privateVar.getDomainAccount(), privateVar.getDomainPassword(), "SMUNET.SMU.CA");

        String emailAddress, name;
        //Create instances of filemanager object and parsers
        FileManager fileManager = new FileManager();
        Parser associationDate = new Parser();
        Parser disassociationDate = new Parser();
        Parser abuseDate = new Parser();

        boolean isCaught;
        File[] matches = fileManager.fileList(emailDirectory);
        String comma = ",";
        String[] cellValues;

        try {
            //Open stream to write a log of caught users to a file
            FileWriter fw = new FileWriter(".\\CopyrightProgram\\CaughtYou.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter writer = new PrintWriter(bw);

            //For every email available
            for (int i = 0; i < matches.length; i++) {
                //Process the email
                isCaught = false;
                Email email = new Email(emailDirectory + "\\" + matches[i].getName());
                //Variables are use to check if the user has associated and disassociated
                AtomicReference<Boolean> isAssociated = new AtomicReference<>();
                AtomicReference<Boolean> isDisassociated = new AtomicReference<>();
                //Send date as text to the parser to get integer values
                abuseDate.parseDate(email.getDate(), email.getTimezone());

                Scanner row = new Scanner(new File(fileManager.netLog()));
                while (row.hasNextLine()) {
                    // Take a full row of excel values and split at commas
                    cellValues = row.nextLine().split(comma);
                    //Checks if the line being read has actual usable data on it
                    if (cellValues.length > 4 && cellValues[1].startsWith("140")) {
                        //Parses log association date to integer values
                        associationDate.parseDate(cellValues[3], "AST");
                        try {
                            //Parses log disassociation date to integer values
                            disassociationDate.parseDate(cellValues[5], "AST");
                        } catch (ArrayIndexOutOfBoundsException ignored) {
                        }

                        if (cellValues[1].equals(email.getIP())) {
                            //Checks if abuse date comes after the IP has been associated
                            isAssociated.set((abuseDate.getDate() > associationDate.getDate()) ||
                                    ((abuseDate.getDate() == associationDate.getDate()) &&
                                            (abuseDate.getTime() >= (associationDate.getTime() - 5))));

                            isDisassociated.set((abuseDate.getDate() > disassociationDate.getDate()) ||
                                    (((abuseDate.getDate() == disassociationDate.getDate()) &&
                                            (abuseDate.getTime() >= disassociationDate.getTime()))));

                            //If the abuse date comes after the association date but not after the disassocation date.
                            if (isAssociated.get() && !isDisassociated.get()) {
                                try {
                                    //Get information from active directory for the s#
                                    NamingEnumeration<SearchResult> result = activeDirectory.searchUser(cellValues[0],
                                            "username", "SMUNET.SMU.CA");
                                    if (result.hasMore()) {
                                        //Get the search results
                                        SearchResult rs = result.next();
                                        Attributes attrs = rs.getAttributes();

                                        //Save email address
                                        emailAddress = attrs.get("mail").toString();
                                        emailAddress = emailAddress.substring(emailAddress.indexOf(":") + 1);

                                        //Save client's name
                                        name = attrs.get("givenname").toString();
                                        name = name.substring(name.indexOf(":") + 1);

                                        Emailer emailOutput = new Emailer(emailAddress, email.getMailBody(),
                                                cellValues[0], name, matches[i].getName(), email.getIP());
                                        emailOutput.notifyClient();

                                        //Print a log of the client caught (Will be replaced by emailed tickets)
                                        writer.println(
                                                "Username: " + cellValues[0] +
                                                        "\nName: " + name +
                                                        "\nEmail Address: " + emailAddress +
                                                        "\nIP: " + email.getIP() +
                                                        "\nMAC Address: " + cellValues[2] +
                                                        "\nDate & Time: " + email.getDate() + email.getTimezone() +
                                                        "\nCase ID: " + email.getID() +
                                                        "\n"
                                        );
                                        isCaught = true;
                                    } else {
                                        System.out.println("No search result found!");
                                    }
                                    break;
                                } catch (PartialResultException ignored) {
                                } catch (NullPointerException ignored) {
                                }
                            } else if (!isAssociated.get())
                                break;
                        }
                    }
                }
                if (!isCaught)
                    writer.println("IP: " + email.getIP() + "\nCase ID: " + email.getID() + " WAS NOT CAUGHT\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Remove all old emails and logs
        fileManager.removeLogs(emailDirectory, "txt");
        fileManager.removeLogs(fileManager.mergedLogs, "csv");
        fileManager.removeLogs(fileManager.networkLogDirectory, "zip");
        System.out.println("Successfully completed!");
        //Closing LDAP Connection
        activeDirectory.closeLdapConnection();
    }
}

