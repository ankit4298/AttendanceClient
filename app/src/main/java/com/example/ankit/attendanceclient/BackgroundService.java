package com.example.ankit.attendanceclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import LocationTracking.GeoFence;
import LocationTracking.LocationTrack;
import SessionHandler.SaveAttendanceContext;
import SessionHandler.SaveSharedPreference;

import static NotificationChannel.App.CHANNEL_1;
import static NotificationChannel.App.CHANNEL_2;

public class BackgroundService extends Service {

    public static final String TAG = "LocationFragment";
    static final int MY_ID = 1991;
    static final String EXTRA_KEY = "STOPKEY";
    static final int EXTRA_VALUE = 1991;


    ProgressDialog progressDialog;
    RequestParams updateparams = new RequestParams();
    RequestParams outparams = new RequestParams();

    LocationTrack locationTrack;
    GeoFence geoFence;
    double currLatitude = 0.0;
    double currLongitude = 0.0;
    int outStatus;

    // thread service
    ScheduledExecutorService getLocationServiceBackground;
    // to restart thread
    ScheduledFuture<?> future;


    @Override
    public void onCreate() {
        super.onCreate();

        geoFence = new GeoFence();

        // set process dialog
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setCancelable(false);

        // init locationTrack object
        locationTrack = new LocationTrack(getApplicationContext());

        // executor service to work in background with only one thread
        getLocationServiceBackground = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // init locationTrack object
        locationTrack = new LocationTrack(getApplicationContext());

        // executor service to work in background with only one thread
        getLocationServiceBackground = Executors.newSingleThreadScheduledExecutor();


        // to stop when clicked on notification stop button
        if (intent != null & intent.getExtras() != null) {
            Log.d(TAG, "called to cancel service");
            int keyCode = intent.getExtras().getInt("STOPKEY");
            Log.d(TAG, "stop key " + keyCode);

            if (keyCode == EXTRA_VALUE) {
                Log.d(TAG, "matched stopkey -- SUCCESS");

                // stop service
                stopSelf();

                // will goto onDestroy and there we will mark forceMarkOut()

            } else {
                Log.d(TAG, "FAILED");
            }
        }

        // create notification for background service
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        // for stop button on notification
        Intent iStopSelf = new Intent(this, BackgroundService.class);
        iStopSelf.putExtra(EXTRA_KEY, EXTRA_VALUE);
        PendingIntent pStopSelf = PendingIntent.getService(this,
                0, iStopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        // create Notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_2)
                .setContentTitle("Attendance Tracking")
                .setContentText("Your Attendance Tracking Started")
                .addAction(R.drawable.inout_ico, "Stop Service", pStopSelf)
                .setSmallIcon(R.drawable.attendance_ico)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();


        startForeground(MY_ID, notification);


        locationTrack = new LocationTrack(getApplicationContext());


        if (locationTrack.canGetLocation()) {


            // Start Background Service
            future = getLocationServiceBackground.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {

                    // check new lat lng in background
                    currLatitude = locationTrack.getLatitude();
                    currLongitude = locationTrack.getLongitude();

                    Log.d(TAG, currLatitude + " " + currLongitude);

                    if (geoFence.checkAgainstBounds(currLatitude, currLongitude)) { // in

                        updateparams.put("eid", SaveSharedPreference.getUserInfo(getApplicationContext()));
                        updateparams.put("latitude", Double.toString(currLatitude));
                        updateparams.put("longitude", Double.toString(currLongitude));

                        updateIntoServer();

                        Log.d(TAG, "updated in server for in");


                    } else {    // out

                        // TODO : set counter_out++ LIMIT 3 ;   -- done

                        Log.d(TAG+"time", "mark time");

                        outStatus = SaveAttendanceContext.getOutStatus(getApplicationContext());
                        Log.d(TAG, "outstatus: " + outStatus);

                        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vibrator.vibrate(500);
                        }
                        setUserOutNotification();


                        outStatus += 1;

                        updateparams.put("eid", SaveSharedPreference.getUserInfo(getApplicationContext()));
                        updateparams.put("latitude", Double.toString(currLatitude));
                        updateparams.put("longitude", Double.toString(currLongitude));

                        updateIntoServer();

                        Log.d(TAG, "updated in server for out");

                        SaveAttendanceContext.updateOUTStatus(getApplicationContext(), outStatus);
                        if (outStatus > 3) {
                            outStatus = 3;
                            forceMarkOUT();
                        }
                    }


                }
            }, 5, 10, TimeUnit.SECONDS);


        } else {    // invoke if GPS is disabled
            locationTrack.showSettingsAlert();
        }


        // Depending if we want to restart the service if the system kills it,
        // we return either START_STICKY, START_NOT_STICKY or START_REDELIVER_INTENT from onStartCommand.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        future.cancel(true);
        forceMarkOUT();
        Log.d(TAG, "Stopping Background Service");
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void forceMarkOUT() {

        future.cancel(true);
        Log.d(TAG, "stopped by force");
        Log.d(TAG, "Total outs :" + outStatus);

        outparams.put("eid", SaveSharedPreference.getUserInfo(getApplicationContext()));
        outparams.put("latitude", Double.toString(locationTrack.getLatitude()));
        outparams.put("longitude", Double.toString(locationTrack.getLongitude()));
        outparams.put("outstatus", Integer.toString(SaveAttendanceContext.getOutStatus(getApplicationContext())));

        markOutIntoServer();

        // reset outStatus to 0
        SaveAttendanceContext.updateOUTStatus(getApplicationContext(), 0);
        String markingDay = SaveAttendanceContext.getTodaysDay(getApplicationContext());
        SaveAttendanceContext.setFirstAttendanceStatus(getApplicationContext(), false, markingDay);

        locationTrack.stopListener();

    }


    private void setUserOutNotification() {

        Intent openApp = new Intent(getApplicationContext(), NavigationActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, openApp, 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1)
                    .setContentTitle("Attendance Tracking System")
                    .setContentText("You are out of company's premises!")
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(1, notification);

        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    .setContentTitle("Attendance Tracking System")
                    .setContentText("You are out of company's premises!")
                    .setAutoCancel(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(1, mBuilder.build());
        }
    }


    public void updateIntoServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getApplicationContext()) + "/UpdateAttendance";
        client.post(url, updateparams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {


                Log.d(TAG, " successfully updated ");
                processJSONResponseUpdate(response);

            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                progressDialog.hide();

                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "404 error", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "server side error", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Heavy Error", Toast.LENGTH_SHORT).show();
                }

            }


        });

    }

    public void markOutIntoServer() {

        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getApplicationContext()) + "/MarkingOutAttendance";
        client.post(url, outparams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {

                Log.d(TAG, " successfully marked out ");
                processJSONResponseMarkingOut(response);

            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                progressDialog.hide();

                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "404 error", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "server side error", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Heavy Error", Toast.LENGTH_SHORT).show();
                }

            }


        });

    }

    private void processJSONResponseUpdate(String response) {

        String serverResponse;
        try {
            JSONObject jsonObject = new JSONObject(response);

            serverResponse = jsonObject.get("response").toString();

            if (1 != Integer.parseInt(serverResponse)) {
                setUserOutNotification();
            }
        } catch (Exception e) {

        }
    }

    private void processJSONResponseMarkingOut(String response) {

        String serverResponse;
        try {
            JSONObject jsonObject = new JSONObject(response);

            serverResponse = jsonObject.get("response").toString();


            Log.d(TAG, "processJSONResponseMarkingOut: " + serverResponse);


            if (1 == Integer.parseInt(serverResponse)) {
                Toast.makeText(getApplicationContext(), "User Marked Out Successfully !", Toast.LENGTH_LONG).show();
                Log.d(TAG, "processJSONResponseMarkingOut: SUCCESS");
            } else {    // because multiple entries inserted
                Toast.makeText(getApplicationContext(), "Error : Inserted Multiple", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "processJSONResponseMarkingOut: ERROR - inserted multiple");
            }
        } catch (Exception e) {

        }
    }


    public static void saveLogData(Context context) throws IOException {
        String filename = "logcat_" + System.currentTimeMillis() + ".txt";
        File opfile = new File(context.getExternalCacheDir(), filename);

        @SuppressWarnings("unused")
        Process process = Runtime.getRuntime().exec("logcat BackgroundService:V *:S -df " + opfile.getAbsolutePath());
    }

}
