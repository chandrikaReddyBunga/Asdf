package com.gtsr.gtsr.loginModule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gtsr.gtsr.R;

import java.security.MessageDigest;

public class SplashScreen extends AppCompatActivity {
    private static final int REQUEST = 112;
    final static int REQUEST_LOCATION = 199;
    Context mContext = this;
    Handler handler;
   // private GoogleApiClient googleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_scree);
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            String[] PERMISSIONS = {
                   // Manifest.permission.READ_PHONE_STATE,
                  //  Manifest.permission.ACCESS_NETWORK_STATE,
                 //   Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
            };
            if (!hasPermissions(mContext, PERMISSIONS)) {
                //enableLoc();
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST);
            } else {
                checker();
            }
        } else {
            // startActivity(new Intent(getApplicationContext(), LoginViewController.class));
            checker();

        }
    }

    public void checkAppFlow() {
        //Get User Location
        LocationTracker.getInstance().fillContext(getApplicationContext());
        LocationTracker.getInstance().startLocation();
        // GET USer Informaion.

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        // clear the notification area when the app is opened
    }

////////////Multiple Premession and alerts//////

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ////// callNextActivity///////////
                    checkAppFlow();
                } else {
                    Toast.makeText(mContext, "PERMISSIONS Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void checker() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //  StartAnimations();
            checkAppFlow();
        } else {
          //  enableLoc();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                }
            }, 1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    }
                    case Activity.RESULT_OK: {
                        // The user was asked to change settings, but chose not to
                        checkAppFlow();

                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {    //when click on phone backbutton
       /* Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

}
