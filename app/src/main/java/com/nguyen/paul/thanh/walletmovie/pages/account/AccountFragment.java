package com.nguyen.paul.thanh.walletmovie.pages.account;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.fragments.ChangeEmailDialogFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.ChangePasswordDialogFragment;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Fragment holds content for user account information
 */
public class AccountFragment extends Fragment
        implements ChangePasswordDialogFragment.PasswordsAcquireListener,
        ChangeEmailDialogFragment.EmailAcquireListener,
        AccountContract.View {

    public static final String FRAGMENT_TAG = AccountFragment.class.getSimpleName();
    public static final String CHANGE_PASSWORD_DIALOG_TAG = "change_password_dialog_tag";
    public static final String CHANGE_EMAIL_DIALOG_TAG = "change_email_dialog_tag";
    public static final String FACEBOOK_AUTH_PROVIDER_NAME = "facebook.com";
    public static final String GOOGLE_AUTH_PROVIDER_NAME = "google.com";

    private CircleImageView mProfilePhoto;
    private EditText mDisplayName;
    private Button mEditProfileBtn;
    private Button mChangePasswordBtn;
    private Button mChangeEmailBtn;
    private Button mResetPasswordBtn;

    private Context mContext;
    private MainActivity mActivity;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private String currentName;

    private AccountContract.Presenter mPresenter;

    //flag to indicate if the user is signed in using email and password
    private boolean signedInWithEmail;
    private String providerName;
    private ViewGroup mParentView;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPresenter = new AccountPresenter(this);

        if(getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }
        mAuth = FirebaseAuth.getInstance();

        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //set toolbar title
        if(mActivity != null) mActivity.setToolbarTitle(R.string.title_account);

        setDisplayNameTextChange();

        signedInWithEmail = true;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            //set current name
            currentName = currentUser.getDisplayName();
            for(UserInfo profile : currentUser.getProviderData()) {

                String provider = profile.getProviderId();
                if(provider.equals(FACEBOOK_AUTH_PROVIDER_NAME)) {
                    providerName = FACEBOOK_AUTH_PROVIDER_NAME;
                    signedInWithEmail = false;
                } else if(provider.equals(GOOGLE_AUTH_PROVIDER_NAME)) {
                    providerName = GOOGLE_AUTH_PROVIDER_NAME;
                    signedInWithEmail = false;
                }
            }
        } else {
            //remove from back stack to avoid users navigate back to this when not signed in
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentView = container;
        // Inflate the mLayout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mEditProfileBtn = (Button) view.findViewById(R.id.edit_profile_btn);
        mEditProfileBtn.setAlpha(.4f);
        mChangePasswordBtn = (Button) view.findViewById(R.id.change_password_btn);
        mChangeEmailBtn = (Button) view.findViewById(R.id.change_email_btn);
        mResetPasswordBtn = (Button) view.findViewById(R.id.reset_password_btn);

        //listen for edit text focus change and show/hide soft keyboard appropriately
        mDisplayName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    Utils.hideKeyboard(mContext, mDisplayName);
                }
            }
        });

        init();

        setEditProfileClickListener();
        setChangePasswordBtnClickListener();
        setChangeEmailBtnClickListener();
        setResetPasswordBtnClickListener();

        return view;
    }

    private void setDisplayNameTextChange() {
        mDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //check if text is the same and not empty
                if(!currentName.equals(charSequence.toString()) && !TextUtils.isEmpty(charSequence.toString())) {
                    //text does change and different from default, enable edit profile button
                    mEditProfileBtn.setAlpha(1);
                    mEditProfileBtn.setClickable(true);
                    mEditProfileBtn.setEnabled(true);
                } else {
                    //text values are not different from default values, disable edit profile button
                    backToCleanState();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private AlertDialog createAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Alert");
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

    private void setResetPasswordBtnClickListener() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Reset Password");
        builder.setMessage("Are you sure you want to reset your password?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mProgressDialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendEmailResetPassword();
            }
        });

        //create alert dialog
        final AlertDialog alertDialog = builder.create();

        mResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
                if(signedInWithEmail) {
                    alertDialog.show();
                } else {
                    mProgressDialog.dismiss();
                    //show dialog message for now, will implement this action for Facebook, Google provider in the future
                    String message = "The app is currently not supported in changing email or password from third party provider. Please contact " + providerName + " to perform this action!";
                    createAlertDialog(message).show();
                }
            }
        });

    }

    private void setChangeEmailBtnClickListener() {
        mChangeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signedInWithEmail) {
                    openChangeEmailDialog();
                } else {
                    //show dialog message for now, will implement this action for Facebook, Google provider in the future
                    String message = "The app is currently not supported in changing email or password from third party provider. Please contact " + providerName + " to perform this action!";
                    createAlertDialog(message).show();
                }

            }
        });
    }

    private void setChangePasswordBtnClickListener() {

        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signedInWithEmail) {
                    openChangePasswordDialog();
                } else {
                    //show dialog message for now, will implement this action for Facebook, Google provider in the future
                    String message = "The app is currently not supported in changing email or password from third party provider. Please contact " + providerName + " to perform this action!";
                    createAlertDialog(message).show();
                }

            }
        });
    }

    private void sendEmailResetPassword() {
        mPresenter.resetPassword();
    }

    private void openChangeEmailDialog() {
        ChangeEmailDialogFragment dialog = new ChangeEmailDialogFragment();
        //set the dialog to full screen
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.setEmailAcquireListener(this);
        dialog.show(getFragmentManager(), CHANGE_EMAIL_DIALOG_TAG);
    }

    private void openChangePasswordDialog() {
        ChangePasswordDialogFragment dialog = new ChangePasswordDialogFragment();
        //set the dialog to full screen
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.setPasswordsAcquireListener(this);
        dialog.show(getFragmentManager(), CHANGE_PASSWORD_DIALOG_TAG);
    }

    private void init() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            //set display name for signed in user
            mDisplayName.setText(currentUser.getDisplayName());
            //set profile photo for signed in user
            Glide.with(mContext).load(currentUser.getPhotoUrl())
                    .crossFade()
                    .centerCrop()
                    //Glide has some issues with loading vector drawable at the moment
                    .placeholder(R.drawable.ic_account_circle_white_24dp)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.ic_account_circle_white_24dp)
                    .into(mProfilePhoto);

        }
    }

    private void backToCleanState() {
        mEditProfileBtn.setAlpha(.4f);
        mEditProfileBtn.setClickable(false);
        mDisplayName.clearFocus();
    }

    private void setEditProfileClickListener() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Update Display Name");
        builder.setMessage("Are you sure you want to update your display name?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //user canceled, set display back to default
                mDisplayName.setText(currentName);
                backToCleanState();

                dialogInterface.dismiss();
                mProgressDialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgressDialog.show();
                updateDisplayName();
            }
        });

        //create alert dialog
        final AlertDialog alertDialog = builder.create();

        mEditProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                alertDialog.show();
            }
        });
    }

    private void updateDisplayName() {
        final String newDisplayName = mDisplayName.getText().toString().trim();

        mPresenter.updateDisplayName(currentName, newDisplayName);
    }

    /*
     * This callback will be invoked when complete getting necessary details from ChangePasswordDialogFragment
     */
    @Override
    public void onPasswordsAcquire(String oldPassword, String newPassword) {
        mProgressDialog.show();
        mPresenter.updatePassword(oldPassword, newPassword);
    }

    /*
     * This callback will be invoked when complete getting necessary details from ChangeEmailDialogFragment
     */
    @Override
    public void onEmailAcquire(String newEmail, String password) {
        mProgressDialog.show();
        mPresenter.updateEmail(newEmail, password);
    }

    @Override
    public void setDisplayNameText(String displayName) {
        currentName = displayName;
        backToCleanState();
    }

    @Override
    public void showDialogResult(String message) {
        mProgressDialog.dismiss();
        createAlertDialog(message).show();
    }

    @Override
    public void showSnackBarWithResult(String message) {
        mProgressDialog.dismiss();
        Utils.createSnackBar(getResources(), mParentView, message).show();
    }
}
