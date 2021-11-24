package com.siddhi.fleet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Delivered extends AppCompatActivity {
    private ImageView EndRide;
    HttpPost httppost;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="Your Ride Starts.";
    double latitude, longitude, location1, accuracy, speed;




    public void showAlert(){
        Delivered.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Delivered.this);
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
    protected String GHetEmpID()
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
        setContentView(R.layout.activity_delivered);
        requestLocationUpdates();

        EndRide= findViewById(R.id.endride);


        EndRide.setOnClickListener(v ->{
            dialog = ProgressDialog.show(Delivered.this, "",
                    "Validating user...", true);
            new Thread(new Runnable() {
                public void run() {
                    endingRide();
                }
            }).start();
        });
    }
    void endingRide(){
        try{

            String lat=  String.valueOf(latitude);
            String longi= String.valueOf(longitude);
            String acc= String.valueOf(accuracy);
            String spd= String.valueOf(speed);

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://transport.siddhisoftwares.com/api/endRide"); // make sure the url is correct.
            //add your data
            nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("driver_id",GetDriverId()));
            nameValuePairs.add(new BasicNameValuePair("challan_id",GHetEmpID()));
            nameValuePairs.add(new BasicNameValuePair("latitude",lat));
            nameValuePairs.add(new BasicNameValuePair("longitude",longi));
            nameValuePairs.add(new BasicNameValuePair("speed",spd));
            nameValuePairs.add(new BasicNameValuePair("location",address));
            nameValuePairs.add(new BasicNameValuePair("accuracy",acc));


            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);


            Log.i("DeliverResponse" ,response);
            runOnUiThread(new Runnable() {
                public void run() {

                    dialog.dismiss();
                }
            });


            if(response.equalsIgnoreCase("{\"responce\":\"Success\",\"msg\":\"Your Ride Stopped.\"}")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Delivered.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Delivered.this);

                                builder.setMessage(R.string.text6)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //Cleardata();
                                                clearPreferences();
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
            Log.i("Exce" ,"e"+e.getMessage());
        }
    }



    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear com.siddhi.fleet");

        } catch (Exception e) {
            e.printStackTrace();
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
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Location location = locationResult.getLastLocation();



                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();
                    accuracy = locationResult.getLastLocation().getAccuracy();
                    speed = locationResult.getLastLocation().getSpeed();

                    if (location != null) {

                    }
                }
            }, null);
        }
    }
}