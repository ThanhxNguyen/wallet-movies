package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.fragments.HomeFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.ProfileFragment;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Menu mNavMenu;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        //get navigation menu refs for show/hide menu items when authenticating users
        mNavMenu = mNavigationView.getMenu();
        mNavigationView.setNavigationItemSelectedListener(this);
        //set home navigation option selected by default
        if(savedInstanceState == null) {
            onNavigationItemSelected(mNavigationView.getMenu().getItem(0));
        }

        prepareFireBaseAuthListener();

    }

    private void prepareFireBaseAuthListener() {
        //setup listener for authentication changes (when user signin/signout)
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //get authenticated user, if user==null, user is signed out otherwise user is signed in
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null) {
                    Log.d(TAG, "onAuthStateChanged: user signed in");
                    //since user is signed in, disable guest mode
                    SharedPreferences.Editor editor = getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, MODE_PRIVATE).edit();
                    editor.putBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, false);
                    editor.apply();
                    
                    //show/hide navigation menu items appropriately
                    mNavMenu.findItem(R.id.auth).getSubMenu().setGroupVisible(R.id.nav_authenticated_group, true);
                    mNavMenu.findItem(R.id.auth).getSubMenu().findItem(R.id.nav_signin).setVisible(false);

                } else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: user signed out");
                    mNavMenu.findItem(R.id.auth).getSubMenu().setGroupVisible(R.id.nav_authenticated_group, false);
                    mNavMenu.findItem(R.id.auth).getSubMenu().findItem(R.id.nav_signin).setVisible(true);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //listen for auth changes
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            //remove auth listener
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(fm.getBackStackEntryCount() == 0) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                fragment = HomeFragment.newInstance();
                break;
            case R.id.nav_profile:
                fragment = ProfileFragment.newInstance();
                break;
            case R.id.nav_signin:
                //navigate to signin activity
                Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_signout:
                //sign out user
                mAuth.signOut();
                break;
            default:
                fragment = HomeFragment.newInstance();
                break;
        }

        //if fragment is not null, populate fragment content
        if(fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
