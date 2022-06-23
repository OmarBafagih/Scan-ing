package com.example.healthinspector.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.healthinspector.R;
import com.example.healthinspector.databinding.FragmentSearchBinding;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ScanFragment scanFragment;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private static final String TAG = "SearchFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //initializing the scanFragment
        scanFragment = new ScanFragment();
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        //launches a popup to request for User's camera permissions
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                //the camera permissions have been granted, navigate to the scan fragment
                //replace the current fragment with the scan fragment (navigates to scan fragment)
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
            } else {
                //user did not allow Camera permissions

                Toast.makeText(getContext(), "Cannot scan barcode without camera permissions", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "User denied permission");

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //onCLick listener for scan icon imageview
        binding.scanIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request for User's camera permissions
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    //camera permissions are already granted, navigate to the scan fragment
                    //replace the current fragment with the scan fragment (navigates to scan fragment)
                    fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, scanFragment).addToBackStack(null).commit();
                }
                else {
                    //launch the request to the user, since permissions have not been granted yet
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}