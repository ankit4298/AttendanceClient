package com.example.ankit.attendanceclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    TextView editProfile,about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        editProfile=findViewById(R.id.editProfile);
        about=findViewById(R.id.aboutApp);

        editProfile.setOnClickListener(this);
        about.setOnClickListener(this);

    }

    public void onClick(View v){

        switch(v.getId()){
            case R.id.editProfile:
                Toast.makeText(getApplicationContext(),"editProfile",Toast.LENGTH_SHORT).show();
                break;
            case R.id.aboutApp:
                Toast.makeText(getApplicationContext(),"about app",Toast.LENGTH_SHORT).show();
                break;
        }

    }

}
