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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

public class ChangePasswordDialogFragment extends DialogFragment {

    private static final String TAG = "ChangePasswordDialogFra";

    private FormInputValidator mFormValidator;
    private TextInputLayout mOldPasswordWrapper;
    private TextInputLayout mNewPasswordWrapper;
    private TextInputLayout mConfirmPasswordWrapper;

    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmPassword;

    private Button mCancelBtn;
    private Button mProceedBtn;

    private PasswordsAcquireListener mListener;
    private Context mContext;

    public interface PasswordsAcquireListener {
        void onPasswordsAcquire(String oldPassword, String newPassword);
    }

    public ChangePasswordDialogFragment() {
        // Required empty public constructor
    }

    public void setPasswordsAcquireListener(PasswordsAcquireListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mFormValidator = FormInputValidator.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password_dialog, container, false);

        mCancelBtn = (Button) view.findViewById(R.id.negative_btn);
        mProceedBtn = (Button) view.findViewById(R.id.positive_btn);

        mOldPassword = (EditText) view.findViewById(R.id.old_password);
        mNewPassword = (EditText) view.findViewById(R.id.new_password);
        mConfirmPassword = (EditText) view.findViewById(R.id.confirm_password);

        mOldPasswordWrapper = (TextInputLayout) view.findViewById(R.id.old_email);
        mNewPasswordWrapper = (TextInputLayout) view.findViewById(R.id.new_email_wrapper);
        mConfirmPasswordWrapper = (TextInputLayout) view.findViewById(R.id.password_wrapper);

        Window window = getDialog().getWindow();
        if(window != null) {
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //adjust dialog height when keyboard appears
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        setOldPasswordTvTextChange();
        setNewPasswordTvTextChange();
        setConfirmPasswordTvTextChange();

        setProceedBtnClickListener();
        setCancelBtnClickListener();

        return view;
    }

    private void setCancelBtnClickListener() {
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide soft keyboard
                Utils.hideKeyboard(mContext, mCancelBtn);
                getDialog().dismiss();
            }
        });
    }

    private void setProceedBtnClickListener() {
        mProceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide soft keyboard
                Utils.hideKeyboard(mContext, mProceedBtn);

                String oldPass = mOldPassword.getText().toString().trim();
                String newPass = mNewPassword.getText().toString().trim();
                String confirmPass = mConfirmPassword.getText().toString().trim();

                boolean isOldPasswordValid = validatePasswordInput(oldPass, mOldPasswordWrapper);
                boolean isNewPassworValid = validatePasswordInput(newPass, mNewPasswordWrapper);
                boolean isPasswordMatched = confirmPassword(newPass, confirmPass);

                if(isOldPasswordValid && isNewPassworValid && isPasswordMatched) {
                    if(mListener != null) {
                        mListener.onPasswordsAcquire(oldPass, newPass);
                        getDialog().dismiss();
                    } else {
                        Log.d(TAG, "onClick: PasswordsAcquireListener is null");
                    }
                }
            }
        });
    }

    private void setConfirmPasswordTvTextChange() {
        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //clear errors
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

    private void setOldPasswordTvTextChange() {
        mOldPassword.addTextChangedListener(createTextWatcherForPasswordFields(mOldPasswordWrapper));
    }

    private void setNewPasswordTvTextChange() {
        mNewPassword.addTextChangedListener(createTextWatcherForPasswordFields(mNewPasswordWrapper));
    }

    private TextWatcher createTextWatcherForPasswordFields(final TextInputLayout passwordWrapper) {

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validatePasswordInput(charSequence.toString(), passwordWrapper);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
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

    private boolean validatePasswordInput(String passwordInput, TextInputLayout passwordWrapper) {
        if(mFormValidator.isEmpty(passwordInput)) {
            passwordWrapper.setError(getString(R.string.error_field_required));
            return false;
        } else {
            int passwordValidationResult = mFormValidator.isValidPassword(passwordInput);
            switch (passwordValidationResult) {

                case FormInputValidator.MIN_LENGTH_ERROR:
                    passwordWrapper.setError(getString(R.string.error_password_too_short));
                    return false;

                case FormInputValidator.MAX_LENGTH_ERROR:
                    passwordWrapper.setError(getString(R.string.error_password_too_long));
                    return false;

                case FormInputValidator.PASSWORD_OK:
                    passwordWrapper.setError(null);
                    return true;

                default:
                    passwordWrapper.setError(getString(R.string.error_password_fail_validate));
                    return false;
            }
        }
    }

}
