package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.nguyen.paul.thanh.walletmovie.R;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private TextView mEmailTv;
    private TextView mPasswordTv;
    private Button mSigninBtn;
    private Button mSignupBtn;

    //Firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        //set click listener for signin and signup buttons
        setClickListenerForSigninBtn();
        setClicklistenerforSignupBtn();

    }

    private void setClickListenerForSigninBtn() {

        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = mEmailTv.getText().toString().trim();
                String passwordInput = mPasswordTv.getText().toString().trim();

                if(!TextUtils.isEmpty(emailInput) && !TextUtils.isEmpty(passwordInput)) {
                    //sign in user with email and password
                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        //successfully signed in, redirect to MainActivity for now
                                        Toast.makeText(SigninActivity.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                        startActivity(intent);
//                                        finish();

                                    } else {
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        Toast.makeText(SigninActivity.this, "Sign in Failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                } else {
                    //signin operation here
                    Toast.makeText(SigninActivity.this, "Missing required fields", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void setClicklistenerforSignupBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to signup activity
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
