package com.example.ankit.attendanceclient;


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import LocationTracking.GeoFence;
import LocationTracking.LocationTrack;
import SessionHandler.SaveAttendanceContext;
import SessionHandler.SaveSharedPreference;


public class LocationFragment extends Fragment {

    public static final String TAG = "LocationFragment";

    GeoFence geoFence;

    Button locBtn, stopLocBtn;
    TextView latText, longText, attendancePopText;

    LocationTrack locationTrack;
    static boolean TRACK_ON = false;

    ProgressDialog progressDialog;
    RequestParams params = new RequestParams();
    RequestParams updateparams = new RequestParams();

    // thread service
    ScheduledExecutorService getLocationServiceBackground;
    // to restart thread
    ScheduledFuture<?> future;

    // used in Executor Service
    double currLatitude;
    double currLongitude;
    double lastLatitude = 0.0;
    double lastLongitude = 0.0;

    int outStatus;


    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ((NavigationActivity) getActivity()).setActionBarTitle("Mark Location");
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // executor service to work in background with only one thread
        getLocationServiceBackground = Executors.newSingleThreadScheduledExecutor();

        locBtn = view.findViewById(R.id.locBtn);
        stopLocBtn = view.findViewById(R.id.stopLocBtn);
        latText = view.findViewById(R.id.latText);
        longText = view.findViewById(R.id.longText);
        attendancePopText = view.findViewById(R.id.attendancePopText);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        stopLocBtn.setVisibility(View.INVISIBLE);
        attendancePopText.setVisibility(View.INVISIBLE);

        geoFence = new GeoFence();

        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                NavigationActivity nava=new NavigationActivity();
//                nava.serviceInNav(true);


                // TODO : check if todays day and marking day --- if same (user already marked) else (USER MARKS)

//                if(true){
//                    Log.d(TAG, "user already marked");
//                    return;
//                }

                locationTrack = new LocationTrack(getContext());

                if (locationTrack.canGetLocation()) {

                    String markingDay = SaveAttendanceContext.getTodaysDay(getContext());
                    double latitude = locationTrack.getLatitude();
                    double longitude = locationTrack.getLongitude();

                    if (latitude == -1 && longitude == -1) {  // Failed
                        Toast.makeText(getActivity(), "Retriving GPS signal failed", Toast.LENGTH_SHORT).show();
                    } else {    // Success

                        locBtn.setVisibility(View.INVISIBLE);
                        stopLocBtn.setVisibility(View.VISIBLE);
                        attendancePopText.setText("Attendance Tracking Started");
                        attendancePopText.setVisibility(View.VISIBLE);

                        latText.setText(Double.toString(latitude));
                        longText.setText(Double.toString(longitude));


                        if (SaveAttendanceContext.getFirstAttendanceStatus(getContext())) {   // first attendance marked
                            // perform update query
                            Toast.makeText(getContext(), "user already marked", Toast.LENGTH_LONG).show();
                            // jump to 'x' method

                        } else { // mark first attendance (insert query)

                            // mark first attendance
                            SaveAttendanceContext.setFirstAttendanceStatus(getContext(), true, markingDay);
                            // TODO : Mark first attendance here

                            Log.d(TAG, "marking first attendance");
                            params.put("eid", SaveSharedPreference.getUserInfo(getContext()));
                            params.put("latitude", Double.toString(latitude));
                            params.put("longitude", Double.toString(longitude));

                            insertIntoServer();

                        }

                        boolean test5 = SaveAttendanceContext.getFirstAttendanceStatus(getContext());
                        String test6 = SaveAttendanceContext.getMarkedFor(getContext());
                        Log.d(TAG, Boolean.toString(test5));
                        Log.d(TAG, test6);


                        // Start Background Service
                        future = getLocationServiceBackground.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {

                                // check new lat lng in background
                                currLatitude = locationTrack.getLatitude();
                                currLongitude = locationTrack.getLongitude();

                                if (geoFence.checkAgainstBounds(currLatitude, currLongitude)) { // in

                                    if (currLatitude == lastLatitude && currLongitude == lastLongitude) {   // user at same place
                                        // TODO : do not update into DB
                                        Log.d(TAG, "same place");


                                    } else {    // user moved
                                        // TODO : update the DB



                                        lastLatitude = currLatitude;
                                        lastLongitude = currLongitude;
                                        Log.d(TAG, currLatitude + " " + currLongitude);

                                        updateparams.put("eid", SaveSharedPreference.getUserInfo(getContext()));
                                        updateparams.put("latitude", Double.toString(lastLatitude));
                                        updateparams.put("longitude", Double.toString(lastLongitude));
                                        // TODO : send duration

                                        updateIntoServer();

                                        Log.d(TAG, "updated in server");
                                        
                                    }

                                } else {    // out

                                    // TODO : set counter_out++ LIMIT 3 ;

                                    outStatus = SaveAttendanceContext.getOutStatus(getContext());
                                    Log.d(TAG, "outstatus: " + outStatus);


                                    Log.d(TAG, "out");
                                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                    } else {
                                        //deprecated in API 26
                                        vibrator.vibrate(500);
                                    }
                                    setUserOutNotification();


                                    outStatus += 1;
                                    SaveAttendanceContext.updateOUTStatus(getContext(), outStatus);
                                    if (outStatus >= 3) {
                                        outStatus = 3;
                                        forceMarkOUT();
                                    }
                                }


                            }
                        }, 1, 3, TimeUnit.SECONDS);


//                    createNotification();

                    }

                } else {    // invoke if GPS is disabled
                    locationTrack.showSettingsAlert();
                }
            }

        });


        stopLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO : make alert box to prompt user to really mark OUTTIME
                String markingDay = SaveAttendanceContext.getTodaysDay(getContext());
                SaveAttendanceContext.setFirstAttendanceStatus(getContext(), false, markingDay);

                stopLocBtn.setVisibility(View.INVISIBLE);
                locBtn.setVisibility(View.VISIBLE);
                attendancePopText.setText("Attendance Tracking Stopped");

                // cancel service thread
                future.cancel(true);
                Log.d(TAG, "stopped");

                // get outStatus and update to DB
                Log.d(TAG, "outs :" + outStatus);
                // TODO : mark outTime and outStatus in DB if clicked by user

                // reset outStatus to 0
                SaveAttendanceContext.updateOUTStatus(getContext(), 0);

                // Stopping loaction listener
                locationTrack.stopListener();
            }
        });

    }

    private void forceMarkOUT() {

        String markingDay = SaveAttendanceContext.getTodaysDay(getContext());
        SaveAttendanceContext.setFirstAttendanceStatus(getContext(), false, markingDay);

//        stopLocBtn.setVisibility(View.INVISIBLE);
//        locBtn.setVisibility(View.VISIBLE);
//        attendancePopText.setText("Attendance Tracking Stopped");

        future.cancel(true);
        Log.d(TAG, "stopped by force");

        locationTrack.stopListener();

    }

    private void createNotification() {

        Intent openApp = new Intent(getContext(), NavigationActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, openApp, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_my_location_black_24dp)
                .setContentTitle("Attendance Tracking System")
                .setContentText("Your Attendance Tracking has started")
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(1, mBuilder.build());

    }

    private void setUserOutNotification() {

        Intent openApp = new Intent(getContext(), NavigationActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, openApp, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setContentTitle("Attendance Tracking System")
                .setContentText("You are out of company's premises!")
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(5, mBuilder.build());

    }


    public void insertIntoServer() {
        progressDialog.setMessage("Marking Attendance");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getContext()) + "/InsertAttendanceDetails";
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();


                Log.d(TAG, " successfully marked ");
                processJSONResponse(response);

            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                progressDialog.hide();

                if (statusCode == 404) {
                    Toast.makeText(getContext(), "404 error", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getContext(), "server side error", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Heavy Error", Toast.LENGTH_SHORT).show();
                }

            }


        });

    }

    public void updateIntoServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getContext()) + "/UpdateAttendance";
        client.post(url, updateparams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {


                Log.d(TAG, " successfully marked ");
                processJSONResponseUpdate(response);

            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                progressDialog.hide();

                if (statusCode == 404) {
                    Toast.makeText(getContext(), "404 error", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getContext(), "server side error", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Heavy Error", Toast.LENGTH_SHORT).show();
                }

            }


        });

    }

    private void processJSONResponse(String response) {

        String serverResponse;
        try {
            JSONObject jsonObject = new JSONObject(response);

            serverResponse = jsonObject.get("response").toString();

            if (1==Integer.parseInt(serverResponse)) {
                createNotification();
            }else{
                Toast.makeText(getContext(),"Failed to mark attendance",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
    }

    private void processJSONResponseUpdate(String response) {

        String serverResponse;
        try {
            JSONObject jsonObject = new JSONObject(response);

            serverResponse = jsonObject.get("response").toString();

            if (1!=Integer.parseInt(serverResponse)) {
                createNotification();
            }else{
                Toast.makeText(getContext(),"Failed to mark attendance",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
    }

}
