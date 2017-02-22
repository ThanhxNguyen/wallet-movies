package com.nguyen.paul.thanh.walletmovie.pages.account;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Presenter for account page
 */

public class AccountPresenter implements AccountContract.Presenter {

    private static final String SIGN_IN_REQUIRED_ERROR_MESSAGE = "Please sign in to proceed!";
    private AccountContract.View mView;
    private FirebaseAuth mAuth;
    
    public AccountPresenter(AccountContract.View view) {
        mView = view;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void updateDisplayName(String currentName, final String newDisplayName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //make sure user is currently signed in
        if(currentUser != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build();

            if(currentName.equals(newDisplayName)) {
                //display name was not changed
                mView.showDialogResult("Display name was not changed! Please change the display name to update.");
            } else {
                //start updating user profile
                currentUser.updateProfile(profile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    //update current name
//                                    currentName = newDisplayName;
                                    mView.setDisplayNameText(newDisplayName);
                                    //clear display name EditText focus
//                                    backToCleanState();
                                    mView.showDialogResult("Successfully update display name!");
                                } else {
                                    mView.showDialogResult("Error! Could not update display name.");
                                }
                            }
                        });

            }
        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

    @Override
    public void resetPassword() {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mView.showDialogResult("A reset password link has been sent to " + currentUser.getEmail());
                            } else {
                                mView.showDialogResult("Error! Could not send a reset password email. Please try again later!");
                            }
                        }
                    });
        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

    @Override
    public void updateEmail(final String newEmail, final String password) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mView.showDialogResult("Successfully changed email!");
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthRecentLoginRequiredException e) {
                                    //re-authenticate user if user is not recently signed in
                                    reAuthenticateUserToUpdateEmail(newEmail, password);

                                } catch (FirebaseAuthUserCollisionException e) {
                                    mView.showDialogResult("The email has already existed!");
                                } catch (Exception e) {
                                    mView.showDialogResult("Error! Could not update email. Please try another time!");
                                }
                            }
                        }
                    });
        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

    @Override
    public void updatePassword(final String oldPass, final String newPass) {
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {

            currentUser.updatePassword(newPass)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mView.showDialogResult("Successfully changed password!");
                            } else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthRecentLoginRequiredException e) {
                                    //re-authenticate user
                                    reAuthenticateUserToUpdatePassword(oldPass, newPass);

                                } catch (Exception e) {
                                    mView.showDialogResult("Error! Could not update password. Please try another time!");
                                }
                            }
                        }
                    });


        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

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
                                //invoke update password again
                                updateEmail(newEmail, password);
                            } else {
                                mView.showDialogResult("Failed to update email! The password is not correct?");
                            }
                        }
                    });
        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
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
                                //invoke update password again
                                updatePassword(oldPass, newPass);
                            } else {
                                mView.showDialogResult("Failed to update password! The old password is not correct?");
                            }
                        }
                    });
        } else {
            mView.showSnackBarWithResult(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

}
