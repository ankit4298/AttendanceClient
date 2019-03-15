package NotificationChannel;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_1="Attendance channel";
    public static final String CHANNEL_2="Test channel";

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


            NotificationChannel outChannel=new NotificationChannel(
                    CHANNEL_2,
                    "test Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            outChannel.setDescription("test");


            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(outChannel);
        }

    }
}
