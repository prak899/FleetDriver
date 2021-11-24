package com.siddhi.fleet;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class AdminzAuth extends AppCompatActivity {
    EditText UserName, Password;
    ImageButton VerifyAdmin;

    HttpPost httppost;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    String result="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminz_auth);
        init();

        VerifyAdmin.setOnClickListener(v ->{
            dialog = ProgressDialog.show(AdminzAuth.this, "",
                    "Validating user...", true);
            new Thread(new Runnable() {
                public void run() {
                    login();

                }
            }).start();
        });
    }
    void init(){
        UserName= findViewById(R.id.username);
        Password= findViewById(R.id.password);
        VerifyAdmin= findViewById(R.id.verifyAdmin);
    }
    void login(){
        try{

            httpclient=new DefaultHttpClient();
            httppost= new HttpPost("http://transport.siddhisoftwares.com/api/verifyAdmin");
            nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("username",UserName.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("password",Password.getText().toString().trim()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response=httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);


            Log.i("AdminResponse" ,response);
            runOnUiThread(new Runnable() {
                public void run() {
                    dialog.dismiss();
                }
            });
            if(response.equalsIgnoreCase("{\"responce\":true}")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        SaveData(UserName.getText().toString().trim(), Password.getText().toString().trim());
                        Toast.makeText(AdminzAuth.this, "ID Verified", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent i = new Intent(AdminzAuth.this, GPSLog.class);
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
        AdminzAuth.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminzAuth.this);
                builder.setMessage("Wrong Username and Password please recheck, If forgot kindly contact authority")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public void SaveData(String UserName, String Password) {
        //Toast.makeText(getApplicationContext(),  TokenID, Toast.LENGTH_LONG).show();

        SQLiteDatabase sq = openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  systemAdmin where id=1", null);

        if (c1424.moveToFirst()) {

            do {
                ContentValues cr4 = new ContentValues();
                cr4.put("UserName", UserName);
                cr4.put("PassWord", Password);


                sq.insert("systemAdmin", null, cr4);
                sq.update("systemAdmin", cr4, "id='1'", null);

            } while (c1424.moveToNext());
        } else {
            ContentValues cr4 = new ContentValues();
            cr4.put("id", "1");
            cr4.put("UserName", UserName);
            cr4.put("PassWord", Password);

            sq.insert("systemAdmin", null, cr4);
        }
    }
}