package NetworkAbuse;

import javax.mail.Session;
import javax.mail.Store;
import javax.naming.NamingEnumeration;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Calendar.DATE;

/**
 * Created by Damien Robinson
 */
public class Main extends JFrame {

    private static ActiveDirectory activeDirectory;
    private static CredentialManager emailCredentials;
    private static IncomingEmail incomingEmail;
    private static JLabel label, winC, lossC;
    public static JTextArea textArea;
    private static JFrame frame = new JFrame();
    private static Store store;

    public static void main(String[] args) {
        try {
            buildGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildGUI() {

        frame.setTitle("Pirate Catcher v2.4.4 - Author: Damien Robinson");
        frame.setLayout(new FlowLayout());

        JPanel parent = new JPanel();
        parent.setPreferredSize(new Dimension(260, 125));
        parent.setLayout(new FlowLayout());

        JPanel login = new JPanel();
        login.setPreferredSize(new Dimension(250, 125));
        login.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 280));
        panel.setLayout(new FlowLayout());

        JPanel loginFields = new JPanel();
        loginFields.setPreferredSize(new Dimension(240, 50));
        loginFields.setLayout(new FlowLayout());

        label = new JLabel("Monitoring...");
        label.setPreferredSize(new Dimension(230, 30));
        label.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel status = new JLabel("Status: ");
        status.setPreferredSize(new Dimension(50, 15));
        status.setHorizontalAlignment(SwingConstants.LEFT);

        textArea = new JTextArea("Currently Found: \n");
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(280, 220));

        JLabel emailLabel = new JLabel("Username: ");
        emailLabel.setPreferredSize(new Dimension(80, 20));

        JLabel passLabel = new JLabel("Password: ");
        passLabel.setPreferredSize(new Dimension(80, 20));

        JLabel win = new JLabel("WIN: ");
        win.setPreferredSize(new Dimension(55, 15));
        win.setForeground(Color.green);
        winC = new JLabel("0 ");
        winC.setPreferredSize(new Dimension(55, 15));
        winC.setForeground(Color.green);

        JLabel loss = new JLabel("LOSS: ");
        loss.setPreferredSize(new Dimension(55, 15));
        loss.setForeground(Color.RED);
        lossC = new JLabel("0 ");
        lossC.setPreferredSize(new Dimension(55, 15));
        lossC.setForeground(Color.RED);


        JLabel emailConnection = new JLabel("Disconnected");
        emailConnection.setPreferredSize(new Dimension(80, 20));
        emailConnection.setForeground(Color.red);

        JTextField emailAddress = new JTextField("Email Address");
        emailAddress.setPreferredSize(new Dimension(150, 20));
        emailAddress.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (emailAddress.getText().equals("Email Address"))
                    emailAddress.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (emailAddress.getText().equals(""))
                    emailAddress.setText("Email Address");
            }
        });

        JPasswordField emailPasswordField = new JPasswordField();
        emailPasswordField.setPreferredSize(new Dimension(150, 20));
        emailPasswordField.addFocusListener(new FocusListener() {
            //Empties password field when clicked
            @Override
            public void focusGained(FocusEvent e) {
                emailPasswordField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(90, 25));
        loginButton.addActionListener(e -> {

            Thread hilo = new Thread(() -> {
                activeDirectory = new ActiveDirectory("activate", "".toCharArray(), "SMUNET.SMU.CA");
                activeDirectory.isAuthenticated();
                Properties properties = new Properties();
                properties.setProperty("mail.store.protocol", "imaps");
                properties.setProperty("mail.imap.port", "143");

                Session emailSession = Session.getInstance(properties, null);
                try {
                    emailConnection.setText("Connecting...");
                    store = emailSession.getStore();
                    store.connect("outlook.office365.com", emailAddress.getText(), String.valueOf(emailPasswordField.getPassword()));
                    emailConnection.setForeground(Color.green);
                    emailConnection.setText("Connected.");
                    emailCredentials = new CredentialManager(emailAddress.getText(), emailPasswordField.getPassword());
                } catch (Exception e1) {
                    emailConnection.setForeground(Color.red);
                    emailConnection.setText("Failed.");
                }
                if (emailConnection.getForeground().equals(Color.green))
                    try {
                        parent.removeAll();
                        parent.setPreferredSize(new Dimension(320, 300));
                        parent.add(panel);

                        frame.revalidate();
                        frame.repaint();
                        frame.pack();
                        frame.setVisible(true);

                        work();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

            });
            hilo.start();
        });

        WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are you sure you want to exit?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    if (emailConnection.getForeground().equals(Color.green)) {
                        activeDirectory.closeLdapConnection();
                        try {
                            if (store.isConnected())
                                store.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        System.exit(0);
                    } else
                        System.exit(0);
                }
            }
        };

        emailAddress.setText("network.abuse@smu.ca");
        //Main Program components
        panel.add(status);
        panel.add(label);
        panel.add(scroll);
        panel.add(win);
        panel.add(winC);
        panel.add(loss);
        panel.add(lossC);

        loginFields.add(emailLabel);
        loginFields.add(emailAddress);
        loginFields.add(passLabel);
        loginFields.add(emailPasswordField);


        // Email login components' row.
        login.add(status);
        login.add(emailConnection);

        login.add(loginFields);

        // Daylight savings checkbox and login button row.
        login.add(loginButton);

        parent.add(login);

        frame.addWindowListener(exitListener);
        frame.add(parent);
        frame.getRootPane().setDefaultButton(loginButton);
        frame.revalidate();
        frame.repaint();
        frame.pack();
        frame.setVisible(true);
    }

    public static void work() throws Exception {

        int win = 0, loss = 0;
        incomingEmail = new IncomingEmail(store, label);
        File[] clientLogs = incomingEmail.getClientLogs();
        String cellValues[], username, ip, name = "", emailAddress = "", fullname = "", macAddress;
        String[] emailBody = incomingEmail.getEmailBody();
        String[] emailSubject = incomingEmail.getEmailSubject();

        int i = 0;
        //If there are messages
        if (emailBody.length > 0) {

            //For each message
            for (String body : emailBody) {

                //Make new object of outgoing mail
                OutgoingEmail email = new OutgoingEmail(emailCredentials, emailSubject[i], body);
                //Parse email to obtain significant values
                ParseEmail parseEmail = new ParseEmail(body, emailSubject[i]);
                i++;
                label.setText("Searching for " + parseEmail.getIP() + " used @ " + parseEmail.getDate() +
                        parseEmail.getTimezone() + "...");
                boolean isCaught = false;
                //Get an integer representation of the date
                Date pirateDate = parseDate(parseEmail.getDate(), false);

                for (File networkLog : clientLogs) {

                    Scanner row = new Scanner(networkLog);
                    while (row.hasNextLine()) {
                        // Take a full row of excel values and split at commas
                        cellValues = row.nextLine().split(",");

                        //Checks if the line being read has actual usable data on it
                        if (cellValues.length > 4 && cellValues[1].startsWith("140")) {
                            username = cellValues[0];
                            ip = cellValues[1];
                            macAddress = cellValues[2];

                            Date startDate = parseDate(cellValues[3], true);
                            Date endDate = null;
                            try {
                                endDate = parseDate(cellValues[5], true);
                            } catch (ArrayIndexOutOfBoundsException aiobe) {

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(startDate);

                                int hrs = getTime("hrs", cellValues[4]);
                                int mins = getTime("min", cellValues[4]);
                                int sec = getTime("sec", cellValues[4]);

                                calendar.add(Calendar.HOUR_OF_DAY, hrs);
                                calendar.add(Calendar.MINUTE, mins);
                                calendar.add(Calendar.SECOND, sec);

                                endDate = calendar.getTime();


                            }


                            try {
                                if (ip.equals(parseEmail.getIP()) && pirateDate.after(startDate) && pirateDate.before(endDate)) {
                                    //Get information from active directory for the s#
                                    if (username.toLowerCase().startsWith("s") && username.length() == 8) {
                                        try {
                                            NamingEnumeration<SearchResult> result = activeDirectory.searchUser(username,
                                                    "username", "SMUNET.SMU.CA");

                                            if (result.hasMore()) {
                                                //Get the search results
                                                SearchResult rs = result.next();
                                                Attributes attrs = rs.getAttributes();

                                                //Save email address
                                                try {
                                                    emailAddress = attrs.get("mail").toString();
                                                    emailAddress = emailAddress.substring(emailAddress.indexOf(":") + 1);

                                                    //Save client's name
                                                    name = attrs.get("givenname").toString();
                                                    name = name.substring(name.indexOf(":") + 1);

                                                    //Save client's name
                                                    try {
                                                        fullname = attrs.get("displayname").toString();
                                                        fullname = name.substring(fullname.indexOf(":") + 1);
                                                    } catch (StringIndexOutOfBoundsException e) {
                                                        fullname = name;
                                                    }
                                                } catch (NullPointerException ignored) {
                                                }

                                            } else
                                                label.setText("No search result found!");

                                            //Stop searching this file user info was found or not available in AD
                                        } catch (PartialResultException ignored) {
                                        }
                                    } else if (username.equals("")) {
                                        username = "AST_Client";
                                        name = "AST_Client";
                                        fullname = "AST_Client";
                                        emailAddress = "AST_Client";
                                    }

                                    /*
                                    else if (username.startsWith("SMURES")){
                                        name = "Residence Guest";
                                        fullname = name;
                                        emailAddress = "residence@smu.ca";
                                    }
                                    */


                                    /*
                                    else if (username.toLowerCase().equals("cma")){
                                        name = "CMA user";
                                        fullname = name;
                                        emailAddress = "conference.services@smu.ca";
                                    }
                                    */


                                    else {
                                        name = cellValues[0];
                                        fullname = cellValues[0];
                                        emailAddress = cellValues[0];
                                    }


                                    email.setUserDetails(username, name, fullname, emailAddress,
                                            ip, macAddress, pirateDate.toString(), parseEmail.getID());

                                    textArea.append(email.details);

                                    if (emailAddress.contains("@"))
                                        email.notifyClient();

                                    email.makeTicket();
                                    System.out.println(emailBody[i]);
                                    isCaught = true;
                                    win++;
                                    winC.setText(win + "");
                                    break;
                                }
                            } catch (NullPointerException ignored) {
                            }
                        }
                    }
                    if (isCaught)
                        break;
                }
                if (!isCaught) {
                    label.setText("Client was not found...");
                    textArea.append("\nCase ID: " + parseEmail.getID() + " got away...\n");
                    loss++;
                    lossC.setText(loss + " ");
                }
            }
        }

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(DATE, -10);
        Date tenDaysAgo = calendar.getTime();
        for (File log : clientLogs) {
            if (new Date(log.lastModified()).before(tenDaysAgo)) {
                label.setText("Cleaning old logs..." + log.getName());
                log.delete();
            }
        }
        label.setText("Completed.");
    }


    /**
     * @param inputDate String containing the Date.
     * @param isNetLog  flag to define the date format to be used
     * @return Date value of the date string from inputDate
     * @throws ParseException
     */
    public static Date parseDate(String inputDate, boolean isNetLog) throws ParseException {
        DateFormat format = null;
        if (isNetLog) {
            format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        } else if (inputDate.endsWith("Z")) {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
        } else if (inputDate.endsWith("Eastern Time")) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'North American Eastern Time'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("ET"));
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzzz", Locale.ENGLISH);
        }
        return format.parse(inputDate);

    }

    public static int getTime(String timeType, String duration) {
        Matcher time;
        Pattern dateRegex = Pattern.compile("([0-9]{1,2})");
        if (duration.contains(timeType)) {
            if ((duration.indexOf(timeType) - 1) == 0) {
                time = dateRegex.matcher(duration.substring(0, duration.indexOf(timeType)));
            } else {
                time = dateRegex.matcher(duration.substring((duration.indexOf(timeType) - 2), duration.indexOf(timeType)));
            }
            if (time.find())
                return Integer.parseInt(time.group());
        }
        return 0;
    }
}




