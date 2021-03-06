package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPasswordDialogFragment extends DialogFragment {

    private TextInputLayout mEmailWrapper;
    private EditText mEmail;
    private Button mCancelBtn;
    private Button mProceedBtn;

    private FormInputValidator mFormValidator;
    private ResetPasswordAcquireListener mListener;
    private Context mContext;

    public interface ResetPasswordAcquireListener {
        void onResetPasswordAcquire(String email);
    }


    public ResetPasswordDialogFragment() {
        // Required empty public constructor
    }

    public void setResetPasswordAcquireListener( ResetPasswordAcquireListener listener) {
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
        View view = inflater.inflate(R.layout.fragment_reset_password_dialog, container, false);

        mEmail = (EditText) view.findViewById(R.id.email);
        mEmailWrapper = (TextInputLayout) view.findViewById(R.id.email_wrapper);
        mCancelBtn = (Button) view.findViewById(R.id.negative_btn);
        mProceedBtn = (Button) view.findViewById(R.id.positive_btn);


        setTextChangeListenerForEmailInput();
        setCancelBtnClickListener();
        setProceedBtnClickListener();

        Window window = getDialog().getWindow();
        if(window != null) {
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //adjust dialog height when keyboard appears
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

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
                String newEmail = mEmail.getText().toString().trim();

                boolean isEmailValid = validateEmailInput(newEmail);

                if(isEmailValid) {
                    if(mListener != null) {
                        mListener.onResetPasswordAcquire(newEmail);
                        getDialog().dismiss();
                    }
                }
            }
        });
    }

    private void setTextChangeListenerForEmailInput() {
        mEmail.addTextChangedListener(new TextWatcher() {
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

    private boolean validateEmailInput(String emailInput) {
        if(mFormValidator.isEmpty(emailInput)) {
            //email field is empty
            mEmailWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidEmail(emailInput)) {
            //email invalid
            mEmailWrapper.setError(getString(R.string.error_email_invalid));
            return false;

        } else {
            //good to go
            mEmailWrapper.setError(null);
            return true;
        }

    }


}
