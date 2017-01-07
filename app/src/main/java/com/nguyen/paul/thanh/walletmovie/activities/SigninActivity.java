package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nguyen.paul.thanh.walletmovie.R;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private TextView mUsernameTv;
    private TextView mPasswordTv;
    private Button mSigninBtn;
    private Button mSignupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mUsernameTv = (TextView) findViewById(R.id.username);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        //set click listener for signin and signup buttons
        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameInput = mUsernameTv.getText().toString();
                String passwordInput = mPasswordTv.getText().toString();

                if(TextUtils.isEmpty(usernameInput) || TextUtils.isEmpty(passwordInput)) {
                    Toast.makeText(SigninActivity.this, "Required fields missing", Toast.LENGTH_LONG).show();
                } else {
                    //signin operation here
                    Toast.makeText(SigninActivity.this, "Signing...", Toast.LENGTH_LONG).show();
                }
            }
        });

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
