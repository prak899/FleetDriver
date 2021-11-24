package com.siddhi.fleet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

public class MainActivity2 extends Activity {
    ImageView b;
    EditText et,pass;
    TextView tv;
    HttpPost httppost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        b = (ImageView)findViewById(R.id.personalBtn);
        pass= (EditText)findViewById(R.id.name);


        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainActivity2.this, "",
                        "Validating user...", true);
                new Thread(new Runnable() {
                    public void run() {
                        login();
                        loginToFirebase();
                    }
                }).start();
            }
        });
    }
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
                    Log.i("firebase auth success","yes");
                } else {
                    Log.i("firebase auth failed", "no");
                }
            }
        });
    }
    void login(){
        try{

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://transport.siddhisoftwares.com/api/varifyChallan"); // make sure the url is correct.
            //add your data
            nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("challan_no",pass.getText().toString().trim()));
            //nameValuePairs.add(new BasicNameValuePair("challan_id",id));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);

            JSONObject json= null;  //your response
            try {
                json = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
               result = json.getString("challan_id");
                Log.d("Success", result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Log.i("ChallanResponse" ,response);
            runOnUiThread(new Runnable() {
                public void run() {
                    //Toast.makeText(MainActivity2.this, response, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });


            //"{\"responce\": true}"
            if(response.equalsIgnoreCase("{\"responce\":true,\"challan_id\":"+result+"}")){
                runOnUiThread(new Runnable() {
                    public void run() {
                            SaveData(result);
                            Toast.makeText(MainActivity2.this, "ID Verified", Toast.LENGTH_SHORT).show();
                    }
                });
                //startActivity(new Intent(MainActivity2.this, UserLogin.class));
                Intent i = new Intent(MainActivity2.this, UserLogin.class);
                i.putExtra("pass", String.valueOf(pass));
                startActivity(i);
                finish();
            }else{
                showAlert();
            }

        }catch(Exception e){
            dialog.dismiss();
            Log.i("Exce" ,"e"+e.getMessage());
        }
    }
    public void showAlert(){
        MainActivity2.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
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

    public void SaveData(String UserName) {
        //Toast.makeText(getApplicationContext(),  TokenID, Toast.LENGTH_LONG).show();

        SQLiteDatabase sq = openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  system where id=1", null);

        if (c1424.moveToFirst()) {

            do {
                ContentValues cr4 = new ContentValues();
                cr4.put("EmpName", UserName);


                sq.insert("system", null, cr4);
                sq.update("system", cr4, "id='1'", null);

            } while (c1424.moveToNext());
        } else {
            ContentValues cr4 = new ContentValues();
            cr4.put("id", "1");
            cr4.put("EmpName", UserName);

            sq.insert("system", null, cr4);
        }
    }
}