package com.example.ankit.attendanceclient;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import SessionHandler.SaveSharedPreference;

public class ForgotPassword extends AppCompatActivity {

    EditText eidForgotPassBox;
    Button passSubmit;
    ProgressDialog progressDialog;
    RequestParams params = new RequestParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        eidForgotPassBox = findViewById(R.id.eidForgotPassBox);
        passSubmit = findViewById(R.id.passSubmitBtn);

        passSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eidForgotPassBox.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Employee ID", Toast.LENGTH_SHORT).show();
                } else {
                    //wirte mailing code here
                }
            }
        });

    }   // onCreate ends

    public void makeHTTPCall() {
        String jsonText = "";
        progressDialog.setMessage("Validating login");
        progressDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("user", "name");
        String url = SaveSharedPreference.getServerURL(getApplicationContext())+"/ for pass uri";
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                progressDialog.hide();
//                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

//                processJSONResponse(response);
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
}
