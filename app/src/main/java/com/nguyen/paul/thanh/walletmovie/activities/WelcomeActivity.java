package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;

public class WelcomeActivity extends AppCompatActivity {

    private Button mSignupBtn;
    private Button mSigninAsGuestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSignupBtn = (Button) findViewById(R.id.signup_btn);
        mSigninAsGuestBtn = (Button) findViewById(R.id.signin_as_guest_btn);

        setClickListenerForSigninBtn();
        setClickListenerForSigninAsGuestBtn();
    }

    private void setClickListenerForSigninBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to sign in page
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
                //remove this activity from back stack because user shouldn't be able to
                //access this page again after the first time by clicking back button
                finish();
            }
        });
    }

    private void setClickListenerForSigninAsGuestBtn() {
        mSigninAsGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //enable guest mode for this user
                SharedPreferences.Editor editor = getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE)
                                                        .edit();
                editor.putBoolean(PreferenceConst.Auth.FIRST_TIME_USER_PREF_KEY, false);
                editor.putBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, true);
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
