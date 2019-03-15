package NotificationChannel;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_1="Attendance channel";
    public static final String CHANNEL_2="Background channel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel serviceChannel=new NotificationChannel(
                    CHANNEL_1,
                    "Attendance Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setDescription("Background Service");


            NotificationChannel backgroundChannel=new NotificationChannel(
                    CHANNEL_2,
                    "Attendance Background Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            backgroundChannel.setDescription("Attendance Background Running Process");


            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(backgroundChannel);
        }

    }
}
