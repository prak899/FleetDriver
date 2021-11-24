package com.siddhi.fleet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://transport.siddhisoftwares.com/api/getMobileOptions";

    ArrayList<HashMap<String, String>> contactList;

    private Button IssueButton;
    HttpPost httppost;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="Your Ride Starts.";
    double latitude, longitude;

    public void showAlert(){
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        setContentView(R.layout.activity_main);

        requestLocationUpdates();
        contactList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);
        IssueButton= findViewById(R.id.issue);

        new GetContacts().execute();

        IssueButton.setOnClickListener(v ->{
            dialog = ProgressDialog.show(MainActivity.this, "",
                    "Validating user...", true);
            new Thread(new Runnable() {
                public void run() {
                    issue();

                }
            }).start();
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    //JSONArray contacts = jsonObj.getJSONArray("getMobileOptions");
                    JSONArray contacts = new JSONArray(jsonStr);
                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("id");
                        String name = c.getString("option");


                       /* // Phone node is JSON Object
                        JSONObject phone = c.getJSONObject("phone");
                        String mobile = phone.getString("mobile");
                        String home = phone.getString("home");
                        String office = phone.getString("office");*/

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("option", name);



                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, contactList, R.layout.list_view, new String[]{"id", "option"}, new int[]{R.id.name, R.id.email});

            lv.setAdapter(adapter);
        }

    }
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = getString(R.string.firebase_path) + "/" + "data";
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    Location location = locationResult.getLastLocation();
                    latitude= locationResult.getLastLocation().getLatitude();
                    longitude= locationResult.getLastLocation().getLongitude();

                    if (location != null) {
                        //Toast.makeText(TrackingService.this, "Location", Toast.LENGTH_SHORT).show();
                        //Log.d(TAG, "location update " + location);
                        Log.d("userlat", "updated" + latitude);
                        ref.setValue(location);
                    }
                }
            }, null);
        }
    }

    void issue(){
        try{

            String lat=  String.valueOf(latitude);
            String longi= String.valueOf(longitude);
            String a="2";

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://l7htech.com/brlrgh/api/stopRide"); // make sure the url is correct.
            //add your data
            nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("challan_id",GHetEmpID()));
            nameValuePairs.add(new BasicNameValuePair("fleet_issue",a));
            nameValuePairs.add(new BasicNameValuePair("latitude",lat));
            nameValuePairs.add(new BasicNameValuePair("longitude",longi));

            //nameValuePairs.add(new BasicNameValuePair("challan_id",id));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);

            /*JSONObject json= null;  //your response
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                result = json.getString("msg");
                Log.d("Success", result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/

            Log.i("Response2" ,response);
            runOnUiThread(new Runnable() {
                public void run() {
                    //Toast.makeText(MainActivity2.this, response, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });


            //"{\"responce\": true}"
            if(response.equalsIgnoreCase("{\"responce\":\"Success\",\"msg\":\"Your Ride Stopped.\"}")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                builder.setMessage("Oohhhhhhh......Your Ride Stopped.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Toast.makeText(MainActivity.this, "Ohhhh You stop", Toast.LENGTH_SHORT).show();
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
}