package NetworkAbuse;

/**
 * Created by Damien Robinson
 *
 * The purpose of this class is to create an object to store credentials.
 */
public class CredentialManager {
    private static String username;
    private static char[] password;

    public CredentialManager(String user, char[] pwd) {
        username = user;
        password = pwd;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return String.valueOf(password);
    }
}

