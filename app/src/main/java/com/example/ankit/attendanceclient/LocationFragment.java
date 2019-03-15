package com.example.ankit.attendanceclient;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;


import LocationTracking.GeoFence;
import LocationTracking.LocationTrack;
import SessionHandler.SaveAttendanceContext;
import SessionHandler.SaveSharedPreference;

import static android.support.v4.content.ContextCompat.startForegroundService;


public class LocationFragment extends Fragment {

    public static final String TAG = "LocationFragment";
    public static String attendancePopTextStarted = "Attendance Tracking Started. Running in Background";
    public static String attendancePopTextStopped = "Attendance Tracking is OFF";


    GeoFence geoFence;

    Button locBtn, stopLocBtn;
    TextView latText, longText, attendancePopText;

    LocationTrack locationTrack;

    ProgressDialog progressDialog;
    RequestParams insertParams = new RequestParams();
    RequestParams outparams = new RequestParams();


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
//        getLocationServiceBackground = Executors.newSingleThreadScheduledExecutor();

        locBtn = view.findViewById(R.id.locBtn);
        stopLocBtn = view.findViewById(R.id.stopLocBtn);
        latText = view.findViewById(R.id.latText);
        longText = view.findViewById(R.id.longText);
        attendancePopText = view.findViewById(R.id.attendancePopText);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        geoFence = new GeoFence();

        if (isMyServiceRunning(BackgroundService.class)) { // service running
            locBtn.setVisibility(View.INVISIBLE);
            stopLocBtn.setVisibility(View.VISIBLE);
            attendancePopText.setText(attendancePopTextStarted);

        } else { // service terminated
            locBtn.setVisibility(View.VISIBLE);
            stopLocBtn.setVisibility(View.INVISIBLE);
            attendancePopText.setText(attendancePopTextStopped);
        }


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

                    if (latitude == 0.0 && longitude == 0.0) {  // Failed
                        Toast.makeText(getActivity(), "Retriving GPS signal failed", Toast.LENGTH_SHORT).show();
                    } else {    // Success

                        locBtn.setVisibility(View.INVISIBLE);
                        stopLocBtn.setVisibility(View.VISIBLE);
                        attendancePopText.setText(attendancePopTextStarted);

                        latText.setText(Double.toString(latitude));
                        longText.setText(Double.toString(longitude));


                        if (SaveAttendanceContext.getFirstAttendanceStatus(getContext())) {   // first attendance marked
                            // perform update query
                            Toast.makeText(getContext(), "user already marked", Toast.LENGTH_LONG).show();
                            // jump to 'x' method

                        } else { // mark first attendance (insert query)

                            // mark first attendance
                            SaveAttendanceContext.setFirstAttendanceStatus(getContext(), true, markingDay);
                            // TODO : check in out and then mark into DB

                            Log.d(TAG, "marking first attendance");
                            insertParams.put("eid", SaveSharedPreference.getUserInfo(getContext()));
                            insertParams.put("latitude", Double.toString(latitude));
                            insertParams.put("longitude", Double.toString(longitude));

                            insertIntoServer();


                        }

                        // start background service
                        Intent serviceIntent = new Intent(getContext(), BackgroundService.class);
                        startForegroundService(getContext(), serviceIntent);


                    }

                } else {    // invoke if GPS is disabled
                    locationTrack.showSettingsAlert();
                }
            }

        });


        stopLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // stopping service
                Intent stopService = new Intent(getContext(), BackgroundService.class);
                getActivity().stopService(stopService);

                stopLocBtn.setVisibility(View.INVISIBLE);
                locBtn.setVisibility(View.VISIBLE);
                attendancePopText.setText(attendancePopTextStopped);

            }
        });


    } // end onViewCreated


    public void insertIntoServer() {
        progressDialog.setMessage("Marking Attendance");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getContext()) + "/InsertAttendanceDetails";
        client.post(url, insertParams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();


                Log.d(TAG, " successfully inserted ");
                processJSONResponseInsert(response);

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

    private void processJSONResponseInsert(String response) {

        String serverResponse;
        try {
            JSONObject jsonObject = new JSONObject(response);

            serverResponse = jsonObject.get("response").toString();

            if (1 == Integer.parseInt(serverResponse)) {

            } else {
                Toast.makeText(getContext(), "Failed to mark attendance", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}