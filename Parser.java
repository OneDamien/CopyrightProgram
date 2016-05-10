/**
 * Parser.java
 * Created by Damien Robinson
 *
 * The purpose of this class is to read the date values from Emails and from the logs
 * and return the values as integers that the program can understand to make comparisons
 */
package CopyrightProgram;

public class Parser {

    private String[] parsedDate;
    private int date;
    private int time;

    /**
     * Takes in a date as a string in many differen formats
     * extracts the individual numbers from the date (parses)
     * @param inputDate
     * @param timezone
     */
    public void parseDate(String inputDate, String timezone) {
        if (timezone.equals("AST")) {
            parsedDate = inputDate.split("(\\s+|:)");
            String month = monthToNum(parsedDate[1]);
            composeDate(parsedDate[7], month, parsedDate[2]);
            composeTime(parsedDate[3], parsedDate[4], parsedDate[5], timezone);
        } else {
            parsedDate = inputDate.split("(-|:|T|Z|\\s+)");
            composeDate(parsedDate[0], parsedDate[1], parsedDate[2]);
            composeTime(parsedDate[3], parsedDate[4], parsedDate[5], timezone);
        }
    }

    /**
     * Sets the parsed string values of the year, month and day as a single whole number
     * @param year
     * @param month
     * @param day
     */
    private void composeDate (String year, String month, String day)
    {
        date = Integer.parseInt(year + month + day);
    }

    /**
     * Sets the parsed string values of the hour minute and second as a single whole number
     * adjusts the number based on the timezone
     * @param hour
     * @param minute
     * @param second
     * @param timezone
     */
    private void composeTime (String hour, String minute, String second, String timezone)
    {
        //If the timezone is Zulu resolve to AST
        if(timezone.equals("Z")) {
            time = (Integer.parseInt(hour + minute + second) - 30000);
            if (time < 0) {
                time = time + 240000;
            }
        }
        else if(timezone.equals("AST")){
            time = Integer.parseInt(hour + minute + second);
        }
        //If the timezone is EST resolve to AST
        else {
            time = (Integer.parseInt(hour + minute + second) + 10000);
            if (time > 240000) {
                time = time - 240000;
            }
        }
    }

    /**
     * Takes the name of the month and returns the month in digits as a string.
     * @param month - The name of the month
     * @return The month number
     */
    private String monthToNum(String month)
    {
        switch (month)
        {
            case "Jan":
                return "01";
            case "Feb":
                return "02";
            case "Mar":
                return "03";
            case "Apr":
                return "04";
            case "May":
                return "05";
            case "Jun":
                return "06";
            case "Jul":
                return "07";
            case "Aug":
                return "08";
            case "Sep":
                return "09";
            case "Oct":
                return "10";
            case "Nov":
                return "11";
            case "Dec":
                return "12";
            default:
                return "00";
        }
    }

    /**
     * This function returns the date as an integer
     * @return - Returns the date
     */
    public int getDate()
    {
        return date;
    }

    /**
     * This function returns the time as an integer
     * @return - Returns the time
     */
    public int getTime()
    {
        return time;
    }
}
