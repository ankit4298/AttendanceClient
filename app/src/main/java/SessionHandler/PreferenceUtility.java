package SessionHandler;

// stores the keys to values
public class PreferenceUtility {

    // Server URL
    public static final String SERVER_URL_KEY = "server_key";

    // User Credentials
    public static final String LOGGED_IN_PREF = "logged_in_status";
    public static final String USERNAME = "loggedout_user"; // username is same as eid

    // User Details
    public static final String FIRST_NAME = "fn";
    public static final String MIDDLE_NAME = "mn";
    public static final String LAST_NAME = "ln";
    public static final String GENDER = "gender";
    public static final String EMAIL = "em";
    public static final String ADDRESS = "addr";
    public static final String PHNO = "phno";

    // Attendance control BIT's
    public static final String FIRST_ATTENDANCE = "false";
    public static final String MARKING_DAY = "curr_date";
    public static final String OUT_STATUS = "0";

    // Remote Attendance Context Variables
    public static final String CENTER_LATITUDE = "";
    public static final String CENTER_LONGITUDE = "";
    public static final String RADIUS = "";

}
