package com.example.ankit.attendanceclient;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;

import SessionHandler.SaveSharedPreference;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class MainActivity extends AppCompatActivity {

    // permission vars
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;


    Button loginBtn;
    EditText userText, passText;
    TextView forgotPassText;
    ProgressDialog progressDialog;
    RequestParams params = new RequestParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for permission
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        loginBtn = findViewById(R.id.loginBtn);
        userText = findViewById(R.id.userText);
        passText = findViewById(R.id.passText);
        forgotPassText = findViewById(R.id.forgotPassText);

        // Check if user is already logged in
        if (SaveSharedPreference.getLoggedStatus(getApplicationContext())) {
            Intent homepage = new Intent(getApplicationContext(), NavigationActivity.class);
            startActivity(homepage);
            finish();
        } else {
            Log.d("testhere", "here");
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname = userText.getText().toString();
                String pass = passText.getText().toString();

                if (uname.equals("") && pass.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Username and Password", Toast.LENGTH_SHORT).show();
                } else {
                    params.put("username", uname);
                    params.put("password", pass);
                    makeHTTPCall();
                }
            }

        });

        forgotPassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
            }
        });


    } // end onCreate

    public void makeHTTPCall() {
        String jsonText = "";
        progressDialog.setMessage("Validating login");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("user", "name");
        String url = SaveSharedPreference.getServerURL(getApplicationContext()) + "/ValidateLogin";

        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();
//                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                processJSONResponse(response);
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


    private void processJSONResponse(String response) {
        int validate, loginstatus;
        String username;

        try {
            JSONObject jsonObject = new JSONObject(response);

            username = jsonObject.get("username").toString();
            validate = Integer.parseInt(jsonObject.get("loginverified").toString());
            loginstatus = Integer.parseInt(jsonObject.get("loginstatus").toString());

            Log.d("testtest", username + "\t" + validate + "\t" + loginstatus);

            validateLogin(username, validate, loginstatus);

        } catch (Exception e) {
            Log.d("json exception", "json exception");
        }

    }


    public void validateLogin(String username, int validate, int loginstatus) {

        if (validate == 1 && loginstatus == 0) {

            Intent homepage = new Intent(getApplicationContext(), NavigationActivity.class);
            homepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);

            SaveSharedPreference.setLoggedInStatus(getApplicationContext(), true, username);

            startActivity(homepage);
            finish();
        } else if (validate == 1 && loginstatus == 1) {
            Toast.makeText(getApplicationContext(), "User already logged in with other device\nContact Administrator for further assistance", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "LOG IN FAILED", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    // permission stuffs

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
