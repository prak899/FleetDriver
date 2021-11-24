package com.siddhi.fleet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity3 extends AppCompatActivity {

    private LinearLayout viewOperator, viewConsumer;

    /*protected String GetAdminUsername()
    {

        SQLiteDatabase sq=openOrCreateDatabase("FleetAdmin", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  systemAdmin where id=1",null);

        if(c1424.moveToFirst()){
            do{

                return c1424.getString(1);

            }while(c1424.moveToNext());
        }
        return "0";
    }

    protected String GetAdminPassword()
    {

        SQLiteDatabase sq=openOrCreateDatabase("FleetAdmin", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  systemAdmin where id=1",null);

        if(c1424.moveToFirst()){
            do{

                return c1424.getString(2);

            }while(c1424.moveToNext());
        }
        return "0";
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        CreateDatabase();

        getpage();
    }

    public void CreateDatabase() {
        SQLiteDatabase sq = openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        //SQLiteDatabase sq1 = openOrCreateDatabase("FleetAdmin", MODE_PRIVATE, null);
        String FP = "CREATE TABLE IF NOT EXISTS system(id INTEGER PRIMARY KEY,EmpName INTEGER , driverid INTEGER)";
        String FP1 = "CREATE TABLE IF NOT EXISTS systemAdmin(id INTEGER PRIMARY KEY,UserName INTEGER, PassWord INTEGER)";
        //table created
        sq.execSQL(FP);
        sq.execSQL(FP1);

    }

    public void getpage() {
        SQLiteDatabase sq=openOrCreateDatabase("Fleet", MODE_PRIVATE, null);
        @SuppressLint("Recycle") Cursor c1424 = sq.rawQuery("SELECT * FROM  system where id=1",null);
        //@SuppressLint("Recycle") Cursor c1424a = sq.rawQuery("SELECT * FROM  systemAdmin where id=1, UserName="+GetAdminUsername()+"PassWord="+GetAdminPassword(),null);
        @SuppressLint("Recycle") Cursor c1424a = sq.rawQuery("SELECT * FROM  systemAdmin where id=1",null);

        if(c1424.moveToFirst()){

            do{
                Intent intent=new Intent(getApplicationContext(), UserLogin.class);
                startActivity(intent);
            }while(c1424.moveToNext());
        }
        else if (c1424a.moveToNext()){
            do{
                Intent intent=new Intent(getApplicationContext(), GPSLog.class);
                startActivity(intent);
            }while(c1424.moveToNext());
        }
        else
        {
            validate();
        }
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        viewOperator = findViewById(R.id.image_operator);
        viewConsumer = findViewById(R.id.image_consumer);
        View btnContinue = findViewById(R.id.view_continue);

        viewConsumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewConsumer.setSelected(!viewConsumer.isSelected());
                viewOperator.setSelected(false);
            }
        });


        viewOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOperator.setSelected(!viewOperator.isSelected());
                viewConsumer.setSelected(false);
            }
        });


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        startMotion();
    }

    private void startMotion() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                MotionLayout motionLayout = findViewById(R.id.start_screen);
                motionLayout.transitionToEnd();
            }
        }, 1500);
    }

    private void validate() {
        try {

            Intent intent;
            if (viewConsumer.isSelected()) {
                startActivity(new Intent(MainActivity3.this, DriverzAuth.class));

            } else if (viewOperator.isSelected()) {

                startActivity(new Intent(MainActivity3.this, AdminzAuth.class));
            } else {

                Toast.makeText(this, "Select One to Continue", Toast.LENGTH_SHORT).show();
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

