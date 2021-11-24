package com.siddhi.fleet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.siddhi.fleet.Scheduler.Service;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserLogin extends Activity {

    private static final int PERMISSIONS_REQUEST = 1;
    Intent mServiceIntent;
    private TrackingService mYourService;
    private ImageView StartRide;

    HttpPost httppost;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="Your Ride Starts.";
    double latitude, longitude;
    private BottomNavigationView bottomNavigationView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {

                case R.id.home:

                     return true;
                case R.id.search:

                    return true;
                case R.id.favorite:
                    startActivity(new Intent(UserLogin.this, MainActivity.class));
                    return true;
                case  R.id.user:
                    startActivity(new Intent(UserLogin.this, Delivered.class));
                    return true;
            }
            return false;
        }
    };

    public void showAlert(){
        UserLogin.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserLogin.this);
                builder.setTitle("Validation Error.");
                builder.setMessage("Challan number not Found.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    protected String GetDriverId()
    {

        SQLiteDatabase sq=openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  system where id=1",null);

        if(c1424.moveToFirst()){
            do{

                return c1424.getString(2);

            }while(c1424.moveToNext());
        }
        return "0";
    }

    protected String GetChallanId()
    {

        SQLiteDatabase sq=openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  system where id=1",null);

        if(c1424.moveToFirst()){
            do{

                return c1424.getString(1);

            }while(c1424.moveToNext());
        }
        return "0";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);


        /*for (Intent intent : POWERMANAGER_INTENTS)
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                startActivity(intent);
                break;
            }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$HighPowerApplicationsActivity"));
        startActivity(intent);
        enableAutoStart();*/
        requestLocationUpdates();


        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            new AlertDialog.Builder(this)
                    .setTitle("Enable Google's Location Services?")
                    .setMessage("You want to enable GPS Services!")
                    .setPositiveButton("Accept", (dialog, id) -> Accepted())
                    .setNegativeButton("Decline", (dialog, id) -> Rejected())
                    .show();
        }

        StartRide= findViewById(R.id.startride);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);



        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation);

        mYourService = new TrackingService();
        mServiceIntent = new Intent(this, mYourService.getClass());


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

        StartRide.setOnClickListener(v ->{

            dialog = ProgressDialog.show(UserLogin.this, "",
                    "Validating user...", true);
            new Thread(new Runnable() {
                public void run() {
                    startingRide();

                }
            }).start();
        });

    }


    private void startTrackerService() {
        startActivity(new Intent(this, com.siddhi.fleet.Scheduler.MainActivity.class));
         //startService(new Intent(this, com.siddhi.fleet.Scheduler.MainActivity.class));
     }
    public void Accepted(){
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        finish();
    }
    public void Rejected(){
        Toast.makeText(this, "Please enable location to be tracked", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start the service when the permission is granted
            //startTrackerService();
        } else {
            finish();
        }
    }

    void startingRide(){
        try{

            String lat=  String.valueOf(latitude);
            String longi= String.valueOf(longitude);

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://transport.siddhisoftwares.com/api/startRide"); // make sure the url is correct.
            //add your data
            nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("driver_id",GetDriverId()));
            nameValuePairs.add(new BasicNameValuePair("challan_id",GetChallanId()));
            nameValuePairs.add(new BasicNameValuePair("latitude",lat));
            nameValuePairs.add(new BasicNameValuePair("longitude",longi));

            //nameValuePairs.add(new BasicNameValuePair("challan_id",id));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);


            Log.i("UserLoginResponse" , String.valueOf(nameValuePairs));
            runOnUiThread(new Runnable() {
                public void run() {

                    dialog.dismiss();
                }
            });


            if(response.equalsIgnoreCase("{\"responce\":\"Success\",\"msg\":\"Your Ride Starts.\"}")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        UserLogin.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(UserLogin.this);

                                builder.setMessage(R.string.text5)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                               startTrackerService();
                                               Snackbar.make(findViewById(R.id.userlay), "You are ready to go", Snackbar.LENGTH_LONG).show();

                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                    }
                });

            }else{
                showAlert();
            }

        }catch(Exception e){
            dialog.dismiss();
            Log.i("Exce" ,"ex"+e.getMessage());
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();

                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();


                    if (location != null) {
                    }
                }
            }, null);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };

    private void enableAutoStart() {
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            new MaterialDialog.Builder(UserLogin.this).title("Enable AutoStart")
                    .content(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {
            new MaterialDialog.Builder(UserLogin.this).title("Enable AutoStart")
                    .content(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.letv.android.letvsafe",
                                    "com.letv.android.letvsafe.AutobootManageActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Honor")) {
            new MaterialDialog.Builder(UserLogin.this).title("Enable AutoStart")
                    .content(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
            new MaterialDialog.Builder(UserLogin.this).title("Enable AutoStart")
                    .content(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.coloros.safecenter",
                                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName("com.oppo.safe",
                                            "com.oppo.safe.permission.startup.StartupAppListActivity");
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.coloros.safecenter",
                                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                        startActivity(intent);
                                    } catch (Exception exx) {

                                    }
                                }
                            }
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.contains("vivo")) {
            new MaterialDialog.Builder(UserLogin.this).title("Enable AutoStart")
                    .content(
                            "Please allow AppName to always run in the background.Our app runs in background else our services can't be accesed.")
                    .theme(Theme.LIGHT)
                    .positiveText("ALLOW")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.iqoo.secure",
                                                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                        startActivity(intent);
                                    } catch (Exception exx) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    })
                    .show();
        }
    }
}