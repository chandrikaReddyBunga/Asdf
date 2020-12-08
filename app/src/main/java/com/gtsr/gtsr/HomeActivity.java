package com.gtsr.gtsr;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gtsr.gtsr.dataController.LanguageTextController;
import com.gtsr.gtsr.dataController.UrineTestDataCreatorController;
import com.gtsr.gtsr.database.TestFactorDataController;
import com.gtsr.gtsr.database.TestFactors;
import com.gtsr.gtsr.database.UrineResultsDataController;
import com.gtsr.gtsr.testModule.PairDeviceViewController;
import com.gtsr.gtsr.testModule.PastResultsActivity;
import com.gtsr.gtsr.testModule.TestActivity;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCConnectionHelper;

import java.util.ArrayList;
public class HomeActivity extends AppCompatActivity {
    RecyclerView resultRecyclerView;
    ResultRecyclerAdapter recyclerAdapter;
    ImageView  imgChart;
    RelativeLayout imgTextNow;
    ArrayList<TestFactors> testFactorsArrayList;
    public static final int ACTION_REQUEST_ENABLE_BT = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        checkLocationPermission();
        checkBluetoothIsEnable();*/
        LanguageTextController.getInstance().loadLanguageTexts();
        UrineTestDataCreatorController.getInstance();
        LanguageTextController.getInstance().fillCOntext(getApplicationContext());
        inti();
    }
    public void checkBluetoothIsEnable() {
        if (SCConnectionHelper.getInstance().mBluetoothAdapter == null) {
            finish();
            return;
        }
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!SCConnectionHelper.getInstance().mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ACTION_REQUEST_ENABLE_BT);
        } else {
            //loadHandler();
            SCConnectionHelper.getInstance().startScan(true);
        }
    }

    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Permission1");
                builder.setMessage("The app needs location permissions. Please grant this permission to continue using the features of the app.");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                });
                builder.setNegativeButton(android.R.string.no, null);
                builder.show();

            }
        } else {
            Log.e("elsecall", "call");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsProviderEnabled, isNetworkProviderEnabled;
            isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Permission");
                builder.setMessage("The app needs location permissions. Please grant this permission to continue using the features of the app.");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.no, null);
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("location permission", "coarse location permission granted");
                    SCConnectionHelper.getInstance().startScan(true);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    SCConnectionHelper.getInstance().startScan(true);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == ACTION_REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else {
            SCConnectionHelper.getInstance().startScan(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void inti() {
        resultRecyclerView = findViewById(R.id.result_recyclerview);
        imgTextNow = findViewById(R.id.img_test_now);
        imgChart = findViewById(R.id.img_chart);
        if(UrineResultsDataController.getInstance().allUrineResults !=null){
            if(UrineResultsDataController.getInstance().allUrineResults.size()>0){
                loadResults();
            }
        }
        imgChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UrineResultsDataController.getInstance().allUrineResults !=null){
                    if(UrineResultsDataController.getInstance().allUrineResults.size()>0){
                        startActivity(new Intent(HomeActivity.this, PastResultsActivity.class));
                    }
                }
            }
        });
        imgTextNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("butoon","call");
              //  throw  new RuntimeException("Test Crash"); // Force a crash
                startActivity(new Intent(getApplicationContext(), PairDeviceViewController.class));
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadResults();
    }
    private void loadResults() {
        UrineResultsDataController.getInstance().fetchAllUrineResults();
        if (UrineResultsDataController.getInstance().currenturineresultsModel != null) {
            testFactorsArrayList = TestFactorDataController.getInstance().fetchTestFactorresults(UrineResultsDataController.getInstance().currenturineresultsModel);
        }
        recyclerAdapter = new ResultRecyclerAdapter(HomeActivity.this, testFactorsArrayList);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        resultRecyclerView.setAdapter(recyclerAdapter);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}