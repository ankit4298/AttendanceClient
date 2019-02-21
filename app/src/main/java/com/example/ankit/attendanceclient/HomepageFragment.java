package com.example.ankit.attendanceclient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomepageFragment extends Fragment {

    // test coords
    double lat = 20.014316;
    double lng = 73.764120;
    LatLng IN_testCoords = new LatLng(20.014325, 73.763782);
    LatLng OUT_testCoords = new LatLng(20.015979, 73.760338);
    // ------

    Button inoutBtn;

    // polygon coordinates
    final List<LatLng> poly = new ArrayList<>();

    public HomepageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Set title bar
        ((NavigationActivity) getActivity()).setActionBarTitle("Homepage");

        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }// onCreated

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inoutBtn = view.findViewById(R.id.inoutBtn);

        // POLYGON Bounds - (GEOFence)
        poly.add(new LatLng(20.014919, 73.762501)); // 1    Top Left
        poly.add(new LatLng(20.015005, 73.764832)); // 2    TR
        poly.add(new LatLng(20.013648, 73.764525)); // 3    BR
        poly.add(new LatLng(20.013790, 73.762158)); // 4    BL
        LatLngBounds bounds = new LatLngBounds(poly.get(3), poly.get(1));

        inoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to get DAY
                Date date=new Date();
                String day=(String)DateFormat.format("dd",date);

                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");
                String currtime=sdf.format(date);

//                if (checkForPolygon(IN_testCoords)) {
//                    Toast.makeText(getActivity(), "Inside", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getActivity(), "Outside", Toast.LENGTH_SHORT).show();
//                }

            }
        });


    }//onviewcreated


    // overloaded methods for checking in out
    public boolean checkForPolygon(double lat,double lng){
        boolean inout = PolyUtil.containsLocation(OUT_testCoords, poly, true);
        return inout;
    }
    public boolean checkForPolygon(LatLng coords){
        boolean inout = PolyUtil.containsLocation(coords, poly, true);
        return inout;
    }

}