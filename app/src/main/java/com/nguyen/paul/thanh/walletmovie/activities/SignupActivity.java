package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private TextView mFirstNameTv;
    private TextView mLastNameTv;
    private TextView mEmailTv;
    private TextView mPasswordTv;
    private TextView mConfirmPasswordTv;
    private Button mSignupBtn;

    //firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        mFirstNameTv = (TextView) findViewById(R.id.first_name);
        mLastNameTv = (TextView) findViewById(R.id.last_name);
        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mConfirmPasswordTv = (TextView) findViewById(R.id.confirm_password);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        setListenerForSignupBtn();
    }

    private void setListenerForSignupBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String firstName = mFirstNameTv.getText().toString().trim();
                final String lastName = mLastNameTv.getText().toString().trim();
                String email = mEmailTv.getText().toString().trim();
                String password = mPasswordTv.getText().toString().trim();

                //sign up user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    //since user signed in, disable guest mode
                                    SharedPreferences prefs = getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();

                                    boolean isFirstTimeUser = prefs.getBoolean(PreferenceConst.Auth.FIRST_TIME_USER_PREF_KEY, true);
                                    boolean isGuest = prefs.getBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, true);

                                    if(isFirstTimeUser) {
                                        editor.putBoolean(PreferenceConst.Auth.FIRST_TIME_USER_PREF_KEY, false);
                                        editor.apply();
                                    }
                                    if(isGuest) {
                                        editor.putBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, false);
                                        editor.apply();
                                    }

                                    Toast.makeText(SignupActivity.this, "Sign up Successfully", Toast.LENGTH_LONG).show();
                                    //update profile info
                                    setUserDisplayNameAfterSignup(firstName, lastName);
                                } else {
                                    Toast.makeText(SignupActivity.this, "Sign up Failed", Toast.LENGTH_LONG).show();
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void setUserDisplayNameAfterSignup(String firstName, String lastName) {
        FirebaseUser user = mAuth.getCurrentUser();

        //make sure user is currently signed in
        if(user != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(firstName + " " + lastName)
                                                    .build();

            //start updating user profile
            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: updated user profile successfully");
                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(intent);
                            }    
                        }
                    });
        }
    }
}
