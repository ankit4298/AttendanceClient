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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import LocationTracking.GeoFence;
import LocationTracking.LocationTrack;
import SessionHandler.SaveAttendanceContext;
import SessionHandler.SaveSharedPreference;

import static android.support.v4.content.ContextCompat.startForegroundService;


public class RemoteLocationFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "remotefragment";
    public static String attendancePopTextStarted = "Attendance Tracking for ";
    public static String attendancePopTextStopped = "Attendance Tracking is OFF";

    public static LatLng centerCoords = null;
    public static double centerLat = 0.0;
    public static double centerLng = 0.0;
    public static double radius = 0.0;
    public static String siteName = "";

    Button remoteLocBtn, stopBtn;
    ImageButton refreshLocBtn;
    Spinner locList;
    TextView attendancePopText;

    LocationTrack locationTrack;
    GeoFence geoFence;

    ProgressDialog progressDialog;
    RequestParams insertParams = new RequestParams();

    String[] items = new String[]{"choose location"};
    ArrayAdapter<String> adapter;

    ArrayList<Integer> locIDList = new ArrayList();
    ArrayList<String> siteList = new ArrayList();
    ArrayList<Double> latList = new ArrayList();
    ArrayList<Double> lngList = new ArrayList();
    ArrayList<Double> radList = new ArrayList();

    public RemoteLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ((NavigationActivity) getActivity()).setActionBarTitle("Remote Attendance");
        return inflater.inflate(R.layout.fragment_remote_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        remoteLocBtn = view.findViewById(R.id.remoteLocBtn);
        stopBtn = view.findViewById(R.id.stopLocBtn);
        refreshLocBtn = view.findViewById(R.id.refreshLocBtn);
        locList = view.findViewById(R.id.locList);
        attendancePopText = view.findViewById(R.id.attendancePopText);

        geoFence = new GeoFence();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        locList.setAdapter(adapter);
        locList.setOnItemSelectedListener(this);

        if (isMyServiceRunning(RemoteBackgroundService.class)) { // service running
            remoteLocBtn.setVisibility(View.INVISIBLE);
            locList.setVisibility(View.INVISIBLE);
            refreshLocBtn.setVisibility(View.INVISIBLE);
            stopBtn.setVisibility(View.VISIBLE);
            attendancePopText.setText(attendancePopTextStarted + siteName);

        } else { // service terminated

            remoteLocBtn.setVisibility(View.VISIBLE);
            locList.setVisibility(View.VISIBLE);
            refreshLocBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
            attendancePopText.setText(attendancePopTextStopped);
        }

        refreshLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getRemoteLocations();


            }
        });


        remoteLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int nullFlag = 0;

                SaveAttendanceContext.updateOUTStatus(getContext(), 0);
                locationTrack = new LocationTrack(getContext());

                if (locationTrack.canGetLocation()) {

                    double latitude = locationTrack.getLatitude();
                    double longitude = locationTrack.getLongitude();
                    if (latitude == 0.0 && longitude == 0.0) {  // Failed
                        Toast.makeText(getActivity(), "Retriving GPS signal failed", Toast.LENGTH_SHORT).show();
                    } else {


                        try {
                            remoteLocBtn.setVisibility(View.INVISIBLE);
                            locList.setVisibility(View.INVISIBLE);
                            refreshLocBtn.setVisibility(View.INVISIBLE);
                            stopBtn.setVisibility(View.VISIBLE);
                            attendancePopText.setText(attendancePopTextStarted + siteName);

                            if (geoFence.isWithinCircle(centerCoords, latitude, longitude, radius)) {

                                Log.d(TAG, "marking first remote attendance");
                                insertParams.put("eid", SaveSharedPreference.getUserInfo(getContext()));
                                insertParams.put("latitude", Double.toString(latitude));
                                insertParams.put("longitude", Double.toString(longitude));

                                insertIntoServer();

                            } else {
                                Toast.makeText(getContext(), "Not inside " + siteName + " area !", Toast.LENGTH_LONG).show();
                                remoteLocBtn.setVisibility(View.VISIBLE);
                                locList.setVisibility(View.VISIBLE);
                                refreshLocBtn.setVisibility(View.VISIBLE);
                                stopBtn.setVisibility(View.INVISIBLE);
                                attendancePopText.setText(attendancePopTextStopped);
                                locationTrack.stopListener();

                                return;
                            }
                        } catch (NullPointerException npe) {
                            nullFlag = 1;
                        }


                        if (nullFlag != 1) {
                            // RemoteBackground service
                            Intent serviceIntent = new Intent(getContext(), RemoteBackgroundService.class);
                            serviceIntent.putExtra("centerLat", centerLat);
                            serviceIntent.putExtra("centerLng", centerLng);
                            serviceIntent.putExtra("radius", radius);

                            startForegroundService(getContext(), serviceIntent);
                        } else {
                            remoteLocBtn.setVisibility(View.VISIBLE);
                            locList.setVisibility(View.VISIBLE);
                            refreshLocBtn.setVisibility(View.VISIBLE);
                            stopBtn.setVisibility(View.INVISIBLE);
                            attendancePopText.setText(attendancePopTextStopped);
                            locationTrack.stopListener();

                            Toast.makeText(getContext(), "Refresh DropDownList and choose site", Toast.LENGTH_LONG).show();
                        }

                    }

                } else {    // invoke if GPS is disabled
                    locationTrack.showSettingsAlert();
                }

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // stopping service
                Intent stopService = new Intent(getContext(), RemoteBackgroundService.class);
                getActivity().stopService(stopService);

                stopBtn.setVisibility(View.INVISIBLE);
                locList.setVisibility(View.VISIBLE);
                refreshLocBtn.setVisibility(View.VISIBLE);
                remoteLocBtn.setVisibility(View.VISIBLE);
                attendancePopText.setText(attendancePopTextStopped);

                Log.d(TAG, "Service Stopped");
            }
        });
    }


    public void getRemoteLocations() {
        progressDialog.setMessage("Fetching Remote Locations ...");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getContext()) + "/GetRemoteSites";
        client.post(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();
                processLocationJSON(response);
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

    private void processLocationJSON(String response) {

        locIDList = new ArrayList();
        siteList = new ArrayList();
        latList = new ArrayList();
        lngList = new ArrayList();
        radList = new ArrayList();
        try {
            JSONObject mainJSON = new JSONObject(response);

            Iterator<String> itr = mainJSON.keys();

            while (itr.hasNext()) {
                String key = itr.next();
                if (mainJSON.get(key) instanceof JSONObject) {

                    int locationID = ((JSONObject) mainJSON.get(key)).getInt("locationID");
                    String site_name = ((JSONObject) mainJSON.get(key)).getString("site_name");
                    double latitude = ((JSONObject) mainJSON.get(key)).getDouble("latitude");
                    double longitude = ((JSONObject) mainJSON.get(key)).getDouble("longitude");
                    double radius = ((JSONObject) mainJSON.get(key)).getDouble("radius");

                    locIDList.add(locationID);
                    siteList.add(site_name);
                    latList.add(latitude);
                    lngList.add(longitude);
                    radList.add(radius);

                }
            }

            Object list[] = siteList.toArray();

            items = Arrays.copyOf(list, list.length, String[].class);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
            locList.setAdapter(adapter);


        } catch (Exception e) {
            Toast.makeText(getContext(), "Fetching Locations Failed . . .", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        for (int i = 0; i < locIDList.size(); i++) {

            if (position == i) {
                double lat = latList.get(i);
                double lng = lngList.get(i);

                centerLat = lat;
                centerLng = lng;
                centerCoords = new LatLng(lat, lng);
                radius = radList.get(i);
                siteName = siteList.get(i);

                Log.d(TAG, "coordssss: " + lat + " " + lng + " " + centerCoords + " " + siteName);


            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void insertIntoServer() {
        progressDialog.setMessage("Marking Attendance");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = SaveSharedPreference.getServerURL(getContext()) + "/InsertRemoteAttendance";
        client.post(url, insertParams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();

                processJSONResponseInsert(response);
                Log.d(TAG, " successfully inserted ");

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

