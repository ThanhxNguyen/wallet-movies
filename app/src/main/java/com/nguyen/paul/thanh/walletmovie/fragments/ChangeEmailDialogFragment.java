package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;

public class ChangeEmailDialogFragment extends DialogFragment {

    private static final String TAG = "ChangeEmailDialogFra";

    private TextView mOldEmail;
    private TextView mNewEmail;
    private TextView mPassword;

    private TextInputLayout mNewEmailWrapper;
    private TextInputLayout mPasswordWrapper;

    private Button mCancelBtn;
    private Button mProceedBtn;

    private FirebaseAuth mAuth;
    private FormInputValidator mFormValidator;
    private EmailAcquireListener mListener;

    public interface EmailAcquireListener {
        void onEmailAcquire(String newEmail, String password);
    }

    public ChangeEmailDialogFragment() {
        // Required empty public constructor
    }

    public void setEmailAcquireListener(EmailAcquireListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAuth = FirebaseAuth.getInstance();
        mFormValidator = FormInputValidator.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_email_dialog, container, false);

        mOldEmail = (TextView) view.findViewById(R.id.old_email);
        mNewEmail = (TextView) view.findViewById(R.id.new_email);
        mPassword = (TextView) view.findViewById(R.id.password);

        mNewEmailWrapper = (TextInputLayout) view.findViewById(R.id.new_email_wrapper);
        mPasswordWrapper = (TextInputLayout) view.findViewById(R.id.password_wrapper);

        mCancelBtn = (Button) view.findViewById(R.id.negative_btn);
        mProceedBtn = (Button) view.findViewById(R.id.positive_btn);

        init();

        return view;
    }

    private void init() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            //set old email text
            mOldEmail.setText(currentUser.getEmail());
        }

        setTextChangeListenerForEmailInput();
        setTextChangeListenerForPasswordInput();

        setProceedBtnClickListener();
        setCancelBtnClickListener();
    }

    private void setCancelBtnClickListener() {
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    private void setProceedBtnClickListener() {
        mProceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newEmail = mNewEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                boolean isPassworValid = validatePasswordInput(password);
                boolean isEmailValid = validateEmailInput(newEmail);

                if(isEmailValid && isPassworValid) {
                    if(mListener != null) {
                        mListener.onEmailAcquire(newEmail, password);
                        getDialog().dismiss();
                    } else {
                        Log.d(TAG, "onClick: EmailAcquireListener is null");
                    }
                }
            }
        });
    }

    private void setTextChangeListenerForEmailInput() {
        mNewEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //validating when user typing new input values
                validateEmailInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setTextChangeListenerForPasswordInput() {
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //validating when user typing new input values
                validatePasswordInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private boolean validateEmailInput(String emailInput) {
        if(mFormValidator.isEmpty(emailInput)) {
            //email field is empty
            mNewEmailWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidEmail(emailInput)) {
            //email invalid
            mNewEmailWrapper.setError(getString(R.string.error_email_invalid));
            return false;

        } else {
            //goog to go
            mNewEmailWrapper.setError(null);
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

}
