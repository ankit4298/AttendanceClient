package com.example.ankit.attendanceclient;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import SessionHandler.SaveSharedPreference;
import SessionHandler.SaveUserDetails;


public class ProfileFragment extends Fragment {


    TextView eidText, nameText, emailText, phnoText, addressText;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ((NavigationActivity) getActivity()).setActionBarTitle("Profile");
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();

        eidText = view.findViewById(R.id.eidText);
        eidText.setText(SaveSharedPreference.getUserInfo(context));

        nameText = view.findViewById(R.id.nameText);
        String fullName = SaveUserDetails.getFirstName(context) + " " + SaveUserDetails.getMiddleName(context) + " " + SaveUserDetails.getLastName(context);
        nameText.setText(fullName);

        emailText = view.findViewById(R.id.emailText);
        emailText.setText(SaveUserDetails.getEmail(context));

        phnoText=view.findViewById(R.id.phnoText);
        phnoText.setText(SaveUserDetails.getPhno(getContext()));

        addressText=view.findViewById(R.id.addressText);
        addressText.setText(SaveUserDetails.getAddress(getContext()));

    }
}
