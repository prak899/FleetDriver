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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class GPSLog extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://transport.siddhisoftwares.com/api/vehicleGPSlog";

    ArrayList<HashMap<String, String>> contactList;

    private Button IssueButton;
    HttpPost httppost;

    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="Your Ride Starts.";
    double latitude, longitude;


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
        setContentView(R.layout.activity_g_p_s_log);

        //handler.postDelayed(refresh,  60 * 1000);
        contactList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);
        IssueButton= findViewById(R.id.issue);

        new GetContacts().execute();

    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(GPSLog.this);
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

                        String driverName = c.getString("driver");
                        String challanNumber = c.getString("challan_no");
                        String status = c.getString("status");
                        String location = c.getString("location");


                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("driverName", "Driver Name:- "+driverName);
                        contact.put("challanNumber", "Challan Number:- "+challanNumber);
                        contact.put("status", "Status:- "+status);
                        contact.put("location", "Location:- "+location);


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
            ListAdapter adapter = new SimpleAdapter(GPSLog.this, contactList, R.layout.admin_list_view, new String[]{"driverName", "challanNumber", "status", "location"}, new int[]{R.id.drivername, R.id.challanno, R.id.status, R.id.location});

            lv.setAdapter(adapter);
        }

    }
    private class GetContacts1 extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONArray contacts = new JSONArray(jsonStr);
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String driverName = c.getString("driver");
                        String challanNumber = c.getString("challan_no");
                        String status = c.getString("status");
                        String location = c.getString("location");

                        HashMap<String, String> contact = new HashMap<>();

                        contact.put("driverName", "Driver Name:- "+driverName);
                        contact.put("challanNumber", "Challan Number:- "+challanNumber);
                        contact.put("status", "Status:- "+status);
                        contact.put("location", "Location:- "+location);

                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            ListAdapter adapter = new SimpleAdapter(GPSLog.this, contactList, R.layout.admin_list_view, new String[]{"driverName", "challanNumber", "status", "location"}, new int[]{R.id.drivername, R.id.challanno, R.id.status, R.id.location});
            lv.setAdapter(adapter);

        }

    }

        /*final Handler handler = new Handler();
        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                new GetContacts1().execute();
                handler.postDelayed(this,  60 * 1000);
                Log.d("Refreshhhed", "called");
            }
        };*/
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);

        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.getItemId();

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                /*dialog = ProgressDialog.show(GPSLog.this, "",
                        "Please Wait", true);
                new Thread(new Runnable() {
                    public void run() {
                        Logout();
                    }
                }).start();
                return true;*/
                Logout();
        }
        return super.onOptionsItemSelected(item);
    }
    public void Logout()
    {
        SQLiteDatabase sq=openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        sq.delete("systemAdmin","id='1'",null);
        Toast.makeText(getApplicationContext(),"Successfully Logout",Toast.LENGTH_LONG).show();
        startActivity(new Intent(GPSLog.this, MainActivity3.class));

    }
}