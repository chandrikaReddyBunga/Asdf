package com.gtsr.gtsr.testModule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gtsr.gtsr.RefreshShowingDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.skyfishjy.library.RippleBackground;

import com.spectrochips.spectrumsdk.FRAMEWORK.SCConnectionHelper;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCTestAnalysis;
import com.spectrochips.spectrumsdk.FRAMEWORK.SpectroCareSDK;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by Abhilash on 2/1/2019.
 */

public class PairDeviceViewController extends AppCompatActivity {
    RelativeLayout relativeScanning, relativeConnect;
    RippleBackground rippleBackground;
    //  public static boolean isFromPairDevcie = false;
    RefreshShowingDialog refreshShowingDialog;
    BluetoothDevice bluetoothDevice;
    public static final int ACTION_REQUEST_ENABLE_BT = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ArrayList<BluetoothDevice> devicesArray = new ArrayList<BluetoothDevice>();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    /* BluetoothDevice bluetoothDevice;
     public static final int ACTION_REQUEST_ENABLE_BT = 1;
     public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
     public static ArrayList<BluetoothDevice> devicesArray = new ArrayList<BluetoothDevice>();*/
    int selectedPosition = -1;
    devicesAdapter adapter;
    RecyclerView recyclerView;
    Button btnNext;
    String deviceStatus = "CONNECT";
    boolean isConnected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairdevice);
        refreshShowingDialog = new RefreshShowingDialog(PairDeviceViewController.this);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS>_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }*/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBluetoothIsEnable();
               // requestSinglePermission();
            }
        }, 1000 * 1);
       /* SpectroCareSDK.getInstance().fillContext(getApplicationContext());
        SCConnectionHelper.getInstance().initializeAdapterAndServcie();
        SCTestAnalysis.getInstance().initializeService();
        activateNotification();*/
        loadRecyclerView();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SpectroCareSDK.getInstance().fillContext(getApplicationContext());
        SCConnectionHelper.getInstance().initializeAdapterAndServcie();
        SCTestAnalysis.getInstance().initializeService();
        activateNotification();
        SCConnectionHelper.getInstance().disconnectWithPeripheral();
        if(bluetoothDevice != null) {
            bluetoothDevice = null;
        }
        devicesArray.clear();
        selectedPosition = -1;
        adapter.notifyDataSetChanged();
        btnNext.setVisibility(View.VISIBLE);
        SCConnectionHelper.getInstance().startScan(true);
        /* SCConnectionHelper.getInstance().disconnectWithPeripheral();
            bluetoothDevice = null;
            devicesArray.clear();
            selectedPosition = -1;
            btnNext.setVisibility(View.VISIBLE);
            loadRecyclerView();
            adapter.notifyDataSetChanged();
            activateNotification();
             SCConnectionHelper.getInstance().startScan(true);*/
        init();
    }

    private void init() {
        // iv = (ImageView) findViewById(R.id.animation);
        relativeScanning = (RelativeLayout) findViewById(R.id.relativeScanning);
        relativeScanning.setVisibility(View.VISIBLE);
        relativeConnect = (RelativeLayout) findViewById(R.id.relativeConnect);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        btnNext = findViewById(R.id.btn_next);
        btnNext.setVisibility(View.GONE);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothDevice = null;
                isConnected = false;
                startActivity(new Intent(PairDeviceViewController.this, DownloadStripViewController.class));
            }
        });
        RelativeLayout back = findViewById(R.id.home);
        RelativeLayout refresh = findViewById(R.id.refresh);
        RelativeLayout back1 = findViewById(R.id.home1);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConnected = false;
                SCConnectionHelper.getInstance().disconnectWithPeripheral();
                bluetoothDevice = null;
                devicesArray.clear();
                selectedPosition = -1;
                adapter.notifyDataSetChanged();
                btnNext.setVisibility(View.GONE);
                SCConnectionHelper.getInstance().startScan(true);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SCConnectionHelper.getInstance().startScan(false);
                finish();
            }
        });
        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SCConnectionHelper.getInstance().startScan(false);
                finish();
            }
        });
    }

    private void loadRecyclerView() {
        adapter = new devicesAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    public void activateNotification() {
        SCConnectionHelper.getInstance().activateScanNotification(new SCConnectionHelper.ScanDeviceInterface() {
            @Override
            public void onSuccessForConnection(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("onSuccessForConnection", "call");
                        refreshShowingDialog.hideRefreshDialog();
                        isConnected=true;
                        btnNext.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        /*if (bluetoothDevice != null) {
                            bluetoothDevice = null;
                            isConnected = false;
                            SCConnectionHelper.getInstance().stopScan();
                            startActivity(new Intent(PairDeviceViewController.this, DownloadStripViewController.class));
                        }*/
                    }
                });
            }

            @Override
            public void onSuccessForScanning(final ArrayList<BluetoothDevice> devcies, boolean msg) {
                Log.e("onSuccessForScanning", "call" + devcies.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (devcies.size() > 0) {
                            devicesArray = devcies;
                            relativeScanning.setVisibility(View.GONE);
                            relativeConnect.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            btnNext.setVisibility(View.GONE);
                        }
                    }
                });


            }

            @Override
            public void onFailureForConnection(String error) {
                refreshShowingDialog.hideRefreshDialog();
                Log.e("onFailureForConnection", "call");
                // SCConnectionHelper.getInstance().disconnectWithPeripheral();
                isConnected = false;
                deviceStatus = "Connect";
                btnNext.setVisibility(View.GONE);
                //  adapter.notifyDataSetChanged();
            }

            @Override
            public void uartServiceClose(String error) {
                //  showMessage("Not support Ble Service");
            }
        });
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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


    public class devicesAdapter extends RecyclerView.Adapter<devicesAdapter.ViewHolder> {
        Context ctx;

        // ArrayList<BluetoothDevice> devicesArray;
        public devicesAdapter(Context ctx/*,ArrayList<BluetoothDevice> devcies*/) {
            this.ctx = ctx;
            //  this.devicesArray=devcies;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name, id;
            ImageView image;
            Button btnConnect;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.testName);
                image = (ImageView) itemView.findViewById(R.id.image);
                id = itemView.findViewById(R.id.txt_id);
                btnConnect = itemView.findViewById(R.id.btn_connect);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanlist, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            BluetoothDevice device = devicesArray.get(position);
            holder.name.setText(device.getName());
            holder.id.setText(device.getAddress());
            holder.btnConnect.setBackgroundResource(R.drawable.btn_gray);
            holder.btnConnect.setText("Connect");
            if (isConnected) {
                Log.e("aaaaaaaaaa", "call" + bluetoothDevice.getAddress());
                selectedPosition=-1;
                if (devicesArray.get(position).getAddress().equals(bluetoothDevice.getAddress())) {
                    holder.btnConnect.setBackgroundResource(R.drawable.btn_gradient);
                    holder.btnConnect.setText("Connected");
                    btnNext.setVisibility(View.VISIBLE);
                }
            } else {
                holder.btnConnect.setBackgroundResource(R.drawable.btn_gray);
                holder.btnConnect.setText("Connect");
                btnNext.setVisibility(View.GONE);
                SCConnectionHelper.getInstance().disconnectWithPeripheral();
            }
            if (selectedPosition == position) {
                selectedPosition = position;
                refreshShowingDialog.showAlert();
                Log.e("selectedpos", "call" + holder.getAdapterPosition());
                bluetoothDevice = devicesArray.get(position);
                SCTestAnalysis.getInstance().mService.connect(bluetoothDevice);
                SCConnectionHelper.getInstance().startScan(false);
            }
            holder.btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isConnected = false;
                    if (selectedPosition != position) {
                        selectedPosition = position;
                        notifyDataSetChanged();
                    } else {
                        selectedPosition = -1;
                        notifyDataSetChanged();
                    }
                }
            });
           /* if (bluetoothDevice != null) {
                  Log.e("aaaaaaaaaa", "call" + bluetoothDevice.getAddress());
                if (devicesArray.get(position).getAddress().equals(bluetoothDevice.getAddress())) {
                    holder.btnConnect.setBackgroundResource(R.drawable.btn_gradient);
                    holder.btnConnect.setText("Connected");
                }
            }*/
        }

        @Override
        public int getItemCount() {
            if (devicesArray.size() > 0) {
                return devicesArray.size();
            } else {
                return 0;
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // if(SCConnectionHelper.getInstance().scanner!=null)
        SCConnectionHelper.getInstance().stopScan();
    }

    /*  @Override
      protected void onDestroy() {
          super.onDestroy();
          // if(SCConnectionHelper.getInstance().scanner!=null)
      }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SCConnectionHelper.getInstance().stopScan();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SCConnectionHelper.getInstance().startScan(false);
        //SCConnectionHelper.getInstance().stopScan();
        // SCTestAnalysis.getInstance().unRegisterReceiver();
        finish();
    }
}


/*
package com.gtsr.gtsr.testModule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gtsr.gtsr.R;
import com.gtsr.gtsr.RefreshShowingDialog;

import com.skyfishjy.library.RippleBackground;

import com.spectrochips.spectrumsdk.FRAMEWORK.SCConnectionHelper;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCTestAnalysis;
import com.spectrochips.spectrumsdk.FRAMEWORK.SpectroCareSDK;

import java.util.ArrayList;

import butterknife.ButterKnife;

*/
/**
 * Created by Abhilash on 2/1/2019.
 *//*


public class PairDeviceViewController extends AppCompatActivity {
    RelativeLayout relativeScanning, relativeConnect;
    RippleBackground rippleBackground;
    //  public static boolean isFromPairDevcie = false;
    RefreshShowingDialog refreshShowingDialog;
    BluetoothDevice bluetoothDevice;
    public static final int ACTION_REQUEST_ENABLE_BT = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ArrayList<BluetoothDevice> devicesArray = new ArrayList<BluetoothDevice>();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    */
/* BluetoothDevice bluetoothDevice;
     public static final int ACTION_REQUEST_ENABLE_BT = 1;
     public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
     public static ArrayList<BluetoothDevice> devicesArray = new ArrayList<BluetoothDevice>();*//*

    int selectedPosition = -1;
    devicesAdapter adapter;
    RecyclerView recyclerView;
    Button btnNext;
    String deviceStatus = "CONNECT";
    boolean isConnected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairdevice);
        refreshShowingDialog = new RefreshShowingDialog(PairDeviceViewController.this);
        */
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }*//*

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBluetoothIsEnable();
                checkLocationPermission();
            }
        }, 1000 * 1);
        SpectroCareSDK.getInstance().fillContext(getApplicationContext());
        SCConnectionHelper.getInstance().initializeAdapterAndServcie();
        SCTestAnalysis.getInstance().initializeService();
        if (SCTestAnalysis.getInstance().mService != null) {
            SCTestAnalysis.getInstance().mService.fillContext(getApplicationContext());
        }
        activateNotification();
        loadRecyclerView();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SpectroCareSDK.getInstance().fillContext(getApplicationContext());
        SCConnectionHelper.getInstance().initializeAdapterAndServcie();
        SCTestAnalysis.getInstance().initializeService();
        activateNotification();
       // SCConnectionHelper.getInstance().disconnectWithPeripheral();
        */
/*if(bluetoothDevice != null) {
            bluetoothDevice = null;
        }*//*

        devicesArray.clear();
        selectedPosition = -1;
        adapter.notifyDataSetChanged();
        btnNext.setVisibility(View.VISIBLE);
        SCConnectionHelper.getInstance().startScan(true);
           */
/* SCConnectionHelper.getInstance().disconnectWithPeripheral();
            bluetoothDevice = null;
            devicesArray.clear();
            selectedPosition = -1;
            btnNext.setVisibility(View.VISIBLE);
            loadRecyclerView();
            adapter.notifyDataSetChanged();
            activateNotification();
             SCConnectionHelper.getInstance().startScan(true);*//*

        init();
    }

    private void init() {
        // iv = (ImageView) findViewById(R.id.animation);
        relativeScanning = (RelativeLayout) findViewById(R.id.relativeScanning);
        relativeScanning.setVisibility(View.VISIBLE);
        relativeConnect = (RelativeLayout) findViewById(R.id.relativeConnect);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        btnNext = findViewById(R.id.btn_next);
        btnNext.setVisibility(View.GONE);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothDevice = null;
                isConnected = false;
                startActivity(new Intent(PairDeviceViewController.this, DownloadStripViewController.class));
            }
        });
        RelativeLayout back = findViewById(R.id.home);
        RelativeLayout refresh = findViewById(R.id.refresh);
        RelativeLayout back1 = findViewById(R.id.home1);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConnected = false;
                SCConnectionHelper.getInstance().disconnectWithPeripheral();
                bluetoothDevice = null;
                devicesArray.clear();
                selectedPosition = -1;
                adapter.notifyDataSetChanged();
                btnNext.setVisibility(View.GONE);
                SCConnectionHelper.getInstance().startScan(true);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SCConnectionHelper.getInstance().startScan(false);
                finish();
            }
        });
        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SCConnectionHelper.getInstance().startScan(false);
                finish();
            }
        });
    }

    private void loadRecyclerView() {
        adapter = new devicesAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    public void activateNotification() {
        SCConnectionHelper.getInstance().activateScanNotification(new SCConnectionHelper.ScanDeviceInterface() {
            @Override
            public void onSuccessForConnection(String msg) {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Log.e("onSuccessForConnection", "call");
                               if (bluetoothDevice != null) {
                                   //bluetoothDevice = null;
                                   refreshShowingDialog.hideRefreshDialog();
                                   isConnected = true;
                                 //  adapter.notifyDataSetChanged();
                                   SCConnectionHelper.getInstance().stopScan();
                                   //  startActivity(new Intent(PairDeviceViewController.this, DownloadStripViewController.class));
                               }

                             */
/*  isConnected = true;
                             refreshShowingDialog.hideRefreshDialog();
                               btnNext.setVisibility(View.VISIBLE);
                               SCConnectionHelper.getInstance().stopScan();*//*

                           }
                       });
                       */
/* if (bluetoothDevice != null) {
                            bluetoothDevice = null;
                            isConnected = true;
                            adapter.notifyDataSetChanged();
                            SCConnectionHelper.getInstance().stopScan();
                          //  startActivity(new Intent(PairDeviceViewController.this, DownloadStripViewController.class));
                        }*//*

            }

            @Override
            public void onSuccessForScanning(final ArrayList<BluetoothDevice> devcies, boolean msg) {
                Log.e("onSuccessForScanning", "call" + devcies.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (devcies.size() > 0) {
                            devicesArray = devcies;
                            relativeScanning.setVisibility(View.GONE);
                            relativeConnect.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            btnNext.setVisibility(View.GONE);
                        }
                    }
                });


            }

            @Override
            public void onFailureForConnection(String error) {
                refreshShowingDialog.hideRefreshDialog();
                Log.e("onFailureForConnection", "call");
                // SCConnectionHelper.getInstance().disconnectWithPeripheral();
                isConnected = false;
                deviceStatus = "Connect";
                btnNext.setVisibility(View.GONE);
                //  adapter.notifyDataSetChanged();
            }

            @Override
            public void uartServiceClose(String error) {
                //  showMessage("Not support Ble Service");
            }
        });
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


    public class devicesAdapter extends RecyclerView.Adapter<devicesAdapter.ViewHolder> {
        Context ctx;

        // ArrayList<BluetoothDevice> devicesArray;
        public devicesAdapter(Context ctx*/
/*,ArrayList<BluetoothDevice> devcies*//*
) {
            this.ctx = ctx;
            //  this.devicesArray=devcies;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView name, id;
            ImageView image;
            Button btnConnect;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.testName);
                image = (ImageView) itemView.findViewById(R.id.image);
                id = itemView.findViewById(R.id.txt_id);
                btnConnect = itemView.findViewById(R.id.btn_connect);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanlist, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            BluetoothDevice device = devicesArray.get(position);
            holder.name.setText(device.getName());
            holder.id.setText(device.getAddress());
            holder.btnConnect.setBackgroundResource(R.drawable.btn_gray);
            holder.btnConnect.setText("Connect");
           */
/* if (isConnected) {
                if (devicesArray.get(position).getAddress().equals(bluetoothDevice.getAddress())) {
                    Log.e("aaaaaaaaaa", "call" + bluetoothDevice.getAddress());
                    holder.btnConnect.setBackgroundResource(R.drawable.btn_gradient);
                    holder.btnConnect.setText("Connected");
                }
            }*//*

            if (bluetoothDevice != null && isConnected) {
                Log.e("aaaaaaaaaa", "call" + bluetoothDevice.getAddress());
                if (devicesArray.get(position).getAddress().equals(bluetoothDevice.getAddress())) {
                    holder.btnConnect.setBackgroundResource(R.drawable.btn_gradient);
                    holder.btnConnect.setText("Connected");
                    btnNext.setVisibility(View.VISIBLE);
                }
            }
            if (selectedPosition == position) {
                selectedPosition = position;
                refreshShowingDialog.showAlert();
                Log.e("selectedpos", "call" + holder.getAdapterPosition());
                bluetoothDevice = devicesArray.get(position);
                SCTestAnalysis.getInstance().mService.connect(bluetoothDevice);
                SCConnectionHelper.getInstance().startScan(false);
            }
            holder.btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition != position) {
                        selectedPosition = position;
                        notifyDataSetChanged();
                    } else {
                        selectedPosition = -1;
                        notifyDataSetChanged();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            if (devicesArray.size() > 0) {
                return devicesArray.size();
            } else {
                return 0;
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // if(SCConnectionHelper.getInstance().scanner!=null)
        SCConnectionHelper.getInstance().stopScan();
    }

    */
/*  @Override
      protected void onDestroy() {
          super.onDestroy();
          // if(SCConnectionHelper.getInstance().scanner!=null)
      }*//*

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SCConnectionHelper.getInstance().stopScan();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SCConnectionHelper.getInstance().startScan(false);
        //SCConnectionHelper.getInstance().stopScan();
        // SCTestAnalysis.getInstance().unRegisterReceiver();
        finish();
    }
}
*/
