package com.example.ankit.attendanceclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import SessionHandler.SaveSharedPreference;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "NavActivity";

    //test
    private static boolean RUN_THREAD = false;// thread service



    TextView navUsernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // get nav_header_navigation.xml header view for accessing their elements
        String eid = SaveSharedPreference.getUserInfo(getApplicationContext());
        View headerView = navigationView.getHeaderView(0);
        navUsernameText = headerView.findViewById(R.id.navUsernameText);
        navUsernameText.setText(eid);


        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.screen_area, new HomepageFragment());
        tx.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment fragment = null;

        int id = item.getItemId();

        switch (id) {
//            case R.id.location_item:
//                fragment = new LocationFragment();
//                break;
            case R.id.profile_item:
                fragment = new ProfileFragment();
                break;
            case R.id.logout_item:
                logoutBox();
                break;
            case R.id.homepage_item:
                fragment = new HomepageFragment();
                break;
            default:
                fragment = new LocationFragment();
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.screen_area, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logoutBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
        builder.setTitle("LOGOUT");
        builder.setMessage("Do you want to logout from the account?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        builder.show();

    }

    private void logout() {

        SaveSharedPreference.setLoggedInStatus(getApplicationContext(), false, "loggedout_user");

        Intent logout = new Intent(NavigationActivity.this, MainActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logout);
        finish();

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }




    public static void serviceInNav(boolean runThread) {
        RUN_THREAD = runThread;
        ScheduledExecutorService getLocationServiceBackground=null;
        // to restart thread
        ScheduledFuture<?> future;
        if (RUN_THREAD) {
            // start background service
            future=getLocationServiceBackground.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    Log.d(TAG, "inside run");

                }
            },1,3,TimeUnit.SECONDS);

        } else {
            // dont
        }
    }

}
