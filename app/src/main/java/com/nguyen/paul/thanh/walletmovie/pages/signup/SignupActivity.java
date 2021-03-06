package com.nguyen.paul.thanh.walletmovie.pages.signup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

public class SignupActivity extends AppCompatActivity implements SignUpContract.View {

    private ProgressDialog mProgressDialog;

    private TextView mFirstNameTv;
    private TextView mLastNameTv;
    private TextView mEmailTv;
    private TextView mPasswordTv;
    private TextView mConfirmPasswordTv;
    private Button mSignupBtn;
    private ConstraintLayout mLayout;

    private TextInputLayout mFirstNameWrapper;
    private TextInputLayout mLastNameWrapper;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;
    private TextInputLayout mConfirmPasswordWrapper;

    private FormInputValidator mFormValidator;

    private SignUpContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFormValidator = FormInputValidator.getInstance();

        mPresenter = new SignUpPresenter(this);

        //create progress dialog
        mProgressDialog = new ProgressDialog(SignupActivity.this, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle(getString(R.string.dialog_user_registration_title));
        mProgressDialog.setMessage(getString(R.string.dialog_user_registration_message));
        mProgressDialog.setCancelable(false);

        mLayout = (ConstraintLayout) findViewById(R.id.signup_form_activity);
        mFirstNameTv = (TextView) findViewById(R.id.first_name);
        mLastNameTv = (TextView) findViewById(R.id.last_name);
        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mConfirmPasswordTv = (TextView) findViewById(R.id.confirm_password);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        mFirstNameWrapper = (TextInputLayout) findViewById(R.id.first_name_wrapper);
        mLastNameWrapper = (TextInputLayout) findViewById(R.id.last_name_wrapper);
        mEmailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper);
        mConfirmPasswordWrapper = (TextInputLayout) findViewById(R.id.confirm_password_wrapper);

        setListenerForSignupBtn();

        //set text change listener for TextView
        setTextChangeListenerForFirstNameTv();
        setTextChangeListenerForLastNameTv();
        setTextChangeListenerForEmailTv();
        setTextChangeListenerForPasswordTv();
        setTextChangeListenerForConfirmPasswordTv();
    }

    private void setTextChangeListenerForConfirmPasswordTv() {
        mConfirmPasswordTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mConfirmPasswordWrapper.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setTextChangeListenerForFirstNameTv() {
        mFirstNameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateFirstNameInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setTextChangeListenerForLastNameTv() {
        mLastNameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateLastNameInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setTextChangeListenerForEmailTv() {
        mEmailTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateEmailInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setTextChangeListenerForPasswordTv() {
        mPasswordTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validatePasswordInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setListenerForSignupBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //hide soft keyboard
                Utils.hideKeyboard(SignupActivity.this, mSignupBtn);

                final String firstName = mFirstNameTv.getText().toString().trim();
                final String lastName = mLastNameTv.getText().toString().trim();
                final String email = mEmailTv.getText().toString().trim();
                final String password = mPasswordTv.getText().toString().trim();
                final String confirmPassword = mConfirmPasswordTv.getText().toString().trim();

                boolean validFirstName = validateFirstNameInput(firstName);
                boolean validLastName = validateLastNameInput(lastName);
                boolean validEmail = validateEmailInput(email);
                boolean validPassword = validatePasswordInput(password);
                boolean passwordMatch = confirmPassword(password, confirmPassword);

                if(validFirstName && validLastName && validEmail && validPassword && passwordMatch) {
                    //show progress dialog
                    mProgressDialog.show();
                    mPresenter.registerUser(firstName, lastName, email, password);
                }

            }
        });
    }

    private AlertDialog createAlertDialogForRegistrationFail(String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignupActivity.this);

        dialogBuilder.setTitle(R.string.dialog_title_registration_fail)
                .setMessage(message);

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //create alert dialog and return it
        return dialogBuilder.create();
    }

    private boolean validateFirstNameInput(String firstName) {
        if(mFormValidator.isEmpty(firstName)) {
            //input empty
            mFirstNameWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidName(firstName)) {
            //not a valid name
            mFirstNameWrapper.setError(getString(R.string.error_name_invalid));
            return false;

        } else {
            mFirstNameWrapper.setError(null);
            return true;
        }
    }

    private boolean validateLastNameInput(String lastName) {
        if(mFormValidator.isEmpty(lastName)) {
            //input empty
            mLastNameWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidName(lastName)) {
            //not a valid name
            mLastNameWrapper.setError(getString(R.string.error_name_invalid));
            return false;

        } else {
            mLastNameWrapper.setError(null);
            return true;
        }
    }

    private boolean validateEmailInput(String emailInput) {
        if(mFormValidator.isEmpty(emailInput)) {
            //email field is empty
            mEmailWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidEmail(emailInput)) {
            mEmailWrapper.setError(getString(R.string.error_email_invalid));
            return false;

        } else {
            mEmailWrapper.setError(null);
            return true;
        }
    }

    private boolean confirmPassword(String password, String confirmPassword) {
        if(mFormValidator.isEmpty(confirmPassword)) {
            mConfirmPasswordWrapper.setError(getString(R.string.error_field_required));
            return false;
        } else if(!confirmPassword.equals(password)) {
            //passwords are not matching
            mConfirmPasswordWrapper.setError(getString(R.string.error_password_not_match));
            return false;
        } else {
            mConfirmPasswordWrapper.setError(null);
            return true;
        }
    }

    private boolean validatePasswordInput(String passwordInput) {
        if(mFormValidator.isEmpty(passwordInput)) {
            mPasswordWrapper.setError(getString(R.string.error_field_required));
            return false;
        } else {
            int passwordValidationResult = mFormValidator.isValidPassword(passwordInput);
            switch (passwordValidationResult) {

                case FormInputValidator.MIN_LENGTH_ERROR:
                    mPasswordWrapper.setError(getString(R.string.error_password_too_short));
                    return false;

                case FormInputValidator.MAX_LENGTH_ERROR:
                    mPasswordWrapper.setError(getString(R.string.error_password_too_long));
                    return false;

                case FormInputValidator.PASSWORD_OK:
                    mPasswordWrapper.setError(null);
                    return true;

                default:
                    mPasswordWrapper.setError(getString(R.string.error_password_fail_validate));
                    return false;
            }
        }
    }

    @Override
    public void showSnackBarWithResult(String message) {
        mProgressDialog.dismiss();
        Utils.createSnackBar(getResources(), mLayout, message).show();
    }

    @Override
    public void showDialogResult(String message) {
        mProgressDialog.dismiss();
        createAlertDialogForRegistrationFail(message).show();
    }

    @Override
    public void redirect(Class<?> redirectTo) {
        mProgressDialog.dismiss();
        startActivity(new Intent(SignupActivity.this, redirectTo));
    }
}
