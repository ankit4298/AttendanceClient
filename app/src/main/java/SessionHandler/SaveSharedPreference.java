package SessionHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static SessionHandler.PreferenceUtility.LOGGED_IN_PREF;
import static SessionHandler.PreferenceUtility.SERVER_URL_KEY;
import static SessionHandler.PreferenceUtility.USERNAME;

public class SaveSharedPreference {

    public static final String SERVER_URL="http://jws-app-appserver.7e14.starter-us-west-2.openshiftapps.com";

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // get Server URL
    public static String getServerURL(Context context) {
        return getPreferences(context).getString(SERVER_URL_KEY, SERVER_URL);
    }

    public static boolean getLoggedStatus(Context context) {
        return getPreferences(context).getBoolean(LOGGED_IN_PREF, false);
    }

    public static String getUserInfo(Context context) {
        return getPreferences(context).getString(USERNAME, "loggedout_user");
    }

    public static void setLoggedInStatus(Context context, boolean loggedIn, String username) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED_IN_PREF, loggedIn);
        editor.putString(USERNAME, username);
        editor.apply();
    }

}
