package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.R;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

public class WelcomeActivity extends AppCompatActivity {

    private Button mSignupBtn;
    private Button mSigninAsGuestBtn;
    private Button mSigninBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        mSignupBtn = (Button) findViewById(R.id.signup_btn);
        mSigninAsGuestBtn = (Button) findViewById(R.id.signin_as_guest_btn);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);

        setClickListenerForSignupBtn();
        setClickListenerForSigninAsGuestBtn();
        setClickListenerForSigninBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
        boolean isFirstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);
        //since this page is only accessible to first time user only, redirect if not
        if(!isFirstTimeUser) {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            //remove from backstack as well
            finish();
        }
    }

    private void setClickListenerForSigninBtn() {
        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to sign in page
                Intent intent = new Intent(WelcomeActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setClickListenerForSignupBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to sign in page
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setClickListenerForSigninAsGuestBtn() {
        mSigninAsGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enable guest mode for this user
                SharedPreferences.Editor editor = getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE)
                                                        .edit();
                editor.putBoolean(FIRST_TIME_USER_PREF_KEY, false);
                editor.putBoolean(GUEST_MODE_PREF_KEY, true);
                editor.apply();

                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                //remove this activity from back stack because user shouldn't be able to
                //access this page again after the first time by clicking back button
                finish();
            }
        });
    }
}
