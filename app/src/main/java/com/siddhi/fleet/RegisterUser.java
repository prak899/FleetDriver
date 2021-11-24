package com.siddhi.fleet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {

    private Button registerButton;
    private EditText UserEmail, UserPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);


        init();
        mAuth= FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email= UserEmail.getText().toString();
        String password= UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Eror", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Eror", Toast.LENGTH_SHORT).show();
        }else {

            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please Wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                SendUserMain();
                                Toast.makeText(RegisterUser.this, "Success", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else {
                                String message= task.getException().toString();
                                Toast.makeText(RegisterUser.this, "Error"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserMain() {
        Intent loginIntent= new Intent(RegisterUser.this, UserLogin.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void init() {
        registerButton= findViewById(R.id.btn_register);

        UserEmail= findViewById(R.id.email);
        UserPassword= findViewById(R.id.password);

        loadingBar= new ProgressDialog(this);
        mAuth= FirebaseAuth.getInstance();
    }
}