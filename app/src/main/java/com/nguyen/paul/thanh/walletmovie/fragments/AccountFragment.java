package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nguyen.paul.thanh.walletmovie.R;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Fragment holds content for user account information
 */
public class AccountFragment extends Fragment
        implements ChangePasswordDialogFragment.PasswordsAcquireListener,
        ChangeEmailDialogFragment.EmailAcquireListener {
    private static final String TAG = "AccountFragment";

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
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private String currentName;

    //flag to indicate if the user is signed in using email and password
    private boolean signedInWithEmail;
    private String providerName;

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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mAuth = FirebaseAuth.getInstance();

        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mEditProfileBtn = (Button) view.findViewById(R.id.edit_profile_btn);
        mEditProfileBtn.setAlpha(.4f);
        mChangePasswordBtn = (Button) view.findViewById(R.id.change_password_btn);
        mChangeEmailBtn = (Button) view.findViewById(R.id.change_email_btn);
        mResetPasswordBtn = (Button) view.findViewById(R.id.reset_password_btn);

        init();

        setDisplayNameTextChange();

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
                    String message = "The app is currently not able to change email or password from third party provider. Please contact " + providerName + " to perform this action!";
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
                    String message = "The app is currently not able to change email or password from third party provider. Please contact " + providerName + " to perform this action!";
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
                    String message = "The app is currently not able to change email or password from third party provider. Please contact " + providerName + " to perform this action!";
                    createAlertDialog(message).show();
                }

            }
        });
    }

    private void sendEmailResetPassword() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgressDialog.dismiss();
                            if(task.isSuccessful()) {
                                createAlertDialog("A reset password link has been sent to " + currentUser.getEmail()).show();
                            } else {
                                createAlertDialog("Error! Could not send a reset password email. Please try again later!").show();
                            }
                        }
                    });
        }
    }

    private void openChangeEmailDialog() {
        ChangeEmailDialogFragment dialog = new ChangeEmailDialogFragment();
        dialog.setEmailAcquireListener(this);
        //prevent user cancel dialog when click outside of dialog
        dialog.setCancelable(false);
//        dialog.show(getActivity().getSupportFragmentManager(), CHANGE_EMAIL_DIALOG_TAG);
        dialog.show(getFragmentManager(), CHANGE_EMAIL_DIALOG_TAG);
    }

    private void openChangePasswordDialog() {
        ChangePasswordDialogFragment dialog = new ChangePasswordDialogFragment();
        dialog.setPasswordsAcquireListener(this);
        //prevent user cancel dialog when click outside of dialog
        dialog.setCancelable(false);
//        dialog.show(getActivity().getSupportFragmentManager(), CHANGE_PASSWORD_DIALOG_TAG);
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
        FirebaseUser currentUser = mAuth.getCurrentUser();

        final String newDisplayName = mDisplayName.getText().toString().trim();


        //make sure user is currently signed in
        if(currentUser != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build();

            if(currentName.equals(newDisplayName)) {
                mProgressDialog.dismiss();
                //display name was not changed
                createAlertDialog("Display name was not changed! Please change the display name to update.").show();
            } else {
                //start updating user profile
                currentUser.updateProfile(profile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: successfully update display name");
                                    mProgressDialog.dismiss();
                                    //update current name
                                    currentName = newDisplayName;
                                    //clear display name EditText focus
                                    backToCleanState();
                                    createAlertDialog("Successfully update display name!").show();
                                } else {
                                    Log.d(TAG, "onComplete: error : " + task.getException().toString());
                                    mProgressDialog.dismiss();
                                    createAlertDialog("Error! Could not update display name.");
                                }
                            }
                        });

            }
        }
    }

    private void updatePassword(final String oldPass, final String newPass) {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

            currentUser.updatePassword(newPass)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: update password successfully");
                                mProgressDialog.dismiss();
                                createAlertDialog("Successfully changed password!").show();
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthRecentLoginRequiredException e) {
                                    Log.d(TAG, "onComplete: Exception: " + e.toString());
                                    //re-authenticate user
                                    reAuthenticateUserToUpdatePassword(oldPass, newPass);

                                } catch (Exception e) {
                                    Log.d(TAG, "onComplete: Exception: " + e.toString());
                                    mProgressDialog.dismiss();
                                    createAlertDialog("Error! Could not update password. Please try another time!");
                                }
                            }
                        }
                    });


        }
    }

    private void reAuthenticateUserToUpdatePassword(final String oldPass, final String newPass) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            AuthCredential credentials = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);

            //re-authenticate
            currentUser.reauthenticate(credentials)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: reauth success update again");
                                    //invoke update password again
                                    updatePassword(oldPass, newPass);
                                } else {
                                    mProgressDialog.dismiss();
                                    createAlertDialog("Failed to update password! The old password is not correct?").show();
                                }
                            }
                        });
        }
    }

    private void updateEmail(final String newEmail, final String password) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.updateEmail(newEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: update email successfully");
                                    mProgressDialog.dismiss();
                                    createAlertDialog("Successfully changed email!").show();
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthRecentLoginRequiredException e) {
                                        Log.d(TAG, "onComplete: Exception: " + e.toString());
                                        //re-authenticate user
                                        reAuthenticateUserToUpdateEmail(newEmail, password);

                                    } catch (FirebaseAuthUserCollisionException e) {
                                        mProgressDialog.dismiss();
                                        createAlertDialog("The email has already existed!").show();
                                    } catch (Exception e) {
                                        Log.d(TAG, "onComplete: Exception: " + e.toString());
                                        mProgressDialog.dismiss();
                                        createAlertDialog("Error! Could not update email. Please try another time!").show();
                                    }
                                }
                            }
                        });
        }
    }

    //should refactor this method to avoid duplication
    //clue: maybe using a flag to indicate
    private void reAuthenticateUserToUpdateEmail(final String newEmail, final String password) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            AuthCredential credentials = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

            //re-authenticate
            currentUser.reauthenticate(credentials)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "onComplete: reauth success update again");
                                //invoke update password again
                                updateEmail(newEmail, password);
                            } else {
//                                if(task.getException() instanceof FirebaseAuthUserCollisionException) {
//                                    mProgressDialog.dismiss();
//                                    createAlertDialog("The email has already existed!").show();
//                                } else {
//                                }
                                mProgressDialog.dismiss();
                                createAlertDialog("Failed to update email! The password is not correct?").show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onPasswordsAcquire(String oldPassword, String newPassword) {
        mProgressDialog.show();
        updatePassword(oldPassword, newPassword);
    }

    @Override
    public void onEmailAcquire(String newEmail, String password) {
        mProgressDialog.show();
        updateEmail(newEmail, password);
    }
}
