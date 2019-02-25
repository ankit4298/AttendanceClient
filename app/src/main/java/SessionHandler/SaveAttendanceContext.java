package SessionHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import static SessionHandler.PreferenceUtility.FIRST_ATTENDANCE;
import static SessionHandler.PreferenceUtility.MARKING_DAY;
import static SessionHandler.PreferenceUtility.OUT_STATUS;

import java.util.Date;

public class SaveAttendanceContext {

    private static Date date=new Date();
    public static String TODAYS_DAY=(String) DateFormat.format("dd", date);

    static SharedPreferences getPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getTodaysDay(Context context){
        return getPreferences(context).getString(TODAYS_DAY,TODAYS_DAY);
    }

    public static boolean getFirstAttendanceStatus(Context context){
        return getPreferences(context).getBoolean(FIRST_ATTENDANCE,false);
    }

    public static String getMarkedFor(Context context){
        return getPreferences(context).getString(MARKING_DAY,"null");
    }

    public static int getOutStatus(Context context){
        return getPreferences(context).getInt(OUT_STATUS,0);
    }

    public static void setFirstAttendanceStatus(Context context, boolean firstAttendance,String curr_date) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(FIRST_ATTENDANCE, firstAttendance);
        editor.putString(MARKING_DAY, curr_date);
        editor.apply();
    }

    public static void updateOUTStatus(Context context,int out){
        SharedPreferences.Editor editor= getPreferences(context).edit();
        editor.putInt(OUT_STATUS,out);
        editor.apply();
    }

}
