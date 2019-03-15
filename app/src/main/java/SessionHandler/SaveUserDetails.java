package SessionHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static SessionHandler.PreferenceUtility.ADDRESS;
import static SessionHandler.PreferenceUtility.EMAIL;
import static SessionHandler.PreferenceUtility.FIRST_NAME;
import static SessionHandler.PreferenceUtility.GENDER;
import static SessionHandler.PreferenceUtility.LAST_NAME;
import static SessionHandler.PreferenceUtility.MIDDLE_NAME;
import static SessionHandler.PreferenceUtility.PHNO;

public class SaveUserDetails {

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserDetails(Context context, String firstname, String middlename, String lastname, String gender, String email, String address, String phno) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(FIRST_NAME, firstname);
        editor.putString(MIDDLE_NAME, middlename);
        editor.putString(LAST_NAME, lastname);
        editor.putString(GENDER, gender);
        editor.putString(EMAIL, email);
        editor.putString(ADDRESS, address);
        editor.putString(PHNO, phno);
        editor.apply();
    }


    // getters for user details
    public static String getFirstName(Context context) {
        return getPreferences(context).getString(FIRST_NAME, null);
    }
    public static String getMiddleName(Context context) {
        return getPreferences(context).getString(MIDDLE_NAME, null);
    }
    public static String getLastName(Context context) {
        return getPreferences(context).getString(LAST_NAME, null);
    }
    public static String getGender(Context context) {
        return getPreferences(context).getString(GENDER, null);
    }

    public static String getEmail(Context context) {
        return getPreferences(context).getString(EMAIL, null);
    }

    public static String getAddress(Context context) {
        return getPreferences(context).getString(ADDRESS, null);
    }
    public static String getPhno(Context context) {
        return getPreferences(context).getString(PHNO, null);
    }

}
