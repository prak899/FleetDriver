package com.siddhi.fleet;

import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import java.util.HashMap;
import java.util.List;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    public int counter=0;

    private Thread mUiThread;
    final Handler mHandler = new Handler();

    double latitude, longitude, location1, accuracy, speed;

    RequestQueue requestQueue;

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
    public void onCreate() {
        super.onCreate();

        requestLocationUpdates();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    private void loginToFirebase() {
        // Authenticate with Firebase, and request location updates
        String email = getString(R.string.firebase_email);
        String password = getString(R.string.firebase_password);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(TrackingService.this, "Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "firebase auth success");
                    requestLocationUpdates();
                } else {
                    Log.d(TAG, "firebase auth failed");
                }
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //final String path = getString(R.string.firebase_path) + "/" + "data";
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();

                    /*double lat = locationResult.getLastLocation().getLatitude();
                    double longi = locationResult.getLastLocation().getLongitude();
                    double location1 = locationResult.getLastLocation().getAltitude();
                    double accuracy = locationResult.getLastLocation().getAccuracy();
                    double speed = locationResult.getLastLocation().getSpeed();*/

                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();
                    location1 = locationResult.getLastLocation().getAltitude();
                    accuracy = locationResult.getLastLocation().getAccuracy();
                    speed = locationResult.getLastLocation().getSpeed();

                    if (location != null) {
                        //Toast.makeText(TrackingService.this, "Location", Toast.LENGTH_SHORT).show();
                        //Log.d(TAG, "location update " + location);
                        Log.d(TAG, "location update " + location);
                        //ref.setValue(location);
                        try {
                            PostOperation();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "com.siddhi.fleet";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //startTimer();
        requestLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

   private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  "+ (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }


    public void PostOperation() throws IOException {
        String lat=  String.valueOf(latitude);
        String longi= String.valueOf(longitude);
        String loc= String.valueOf(location1);
        String acc= String.valueOf(accuracy);
        String spd= String.valueOf(speed);
        requestQueue = Volley.newRequestQueue(this);


        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        //Log.e("LocalAddress",  city+ "" +state+ ""+country+ ""+postalCode+ ""+knownName+ "");


        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://transport.siddhisoftwares.com/api/updateLocation",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("loginoutput", response);


                        if (response.equalsIgnoreCase("{\"responce\":\"Success\",\"msg\":\"Location Updated.\"}")) {

                            Log.d("Succcccc", response);
                            //Toast.makeText(TrackingService.this, "success", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("failed", response);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e("authFail", "response");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();

                // pass your input text


                map.put("challan_id", GHetEmpID());
                map.put("latitude", lat);
                map.put("longitude", longi);

                map.put("location",address);
                map.put("accuracy", acc);
                map.put("speed", spd);


                Log.e("para", map + "");
                return map;
            }

        };
        requestQueue.add(stringRequest);
    }
}