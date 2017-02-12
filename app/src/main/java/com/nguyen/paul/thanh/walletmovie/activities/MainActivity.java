package com.nguyen.paul.thanh.walletmovie.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.database.MovieSearchSuggestionProvider;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.fragments.AboutUsFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.AccountFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.FavouriteMoviesFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.HomeFragment;
import com.nguyen.paul.thanh.walletmovie.fragments.MovieListFragment;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String NETWORK_REQUEST_TAG = "network_request_tag";

    private TextView mToolbarTitle;
    private DrawerLayout mDrawerLayout;
    private Menu mNavMenu;
    private TextView mHeaderDisplayName;
    private TextView mHeaderDisplayEmail;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private NetworkRequest mNetworkRequest;
    private List<Genre> mGenreListFromApi;
    //a flag to indicate the current selected drawer item
    private String currentDrawerItemSelected;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Follow android developer guide for launchMode="singleTop"
         * Reference: https://developer.android.com/guide/topics/search/search-dialog.html
         */
        handleIntent(getIntent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set to false because using custom toolbar title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        mPrefs = getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mNetworkRequest = NetworkRequest.getInstance(this);
        mGenreListFromApi = ((App) getApplication()).getGenreListFromApi();

        if(mGenreListFromApi.size() == 0) {
            String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
            sendRequestToGetGenreList(genreListUrl);
        }

        //determine if this is the first time user accesses the app
        SharedPreferences prefs = getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        boolean isFirstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);
        if(isFirstTimeUser) {
            //user accesses the app for the first time, show welcome page
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }


        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.root_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //get navigation drawer header
        View navHeader = navigationView.getHeaderView(0);
        mHeaderDisplayName = (TextView) navHeader.findViewById(R.id.display_name);
        mHeaderDisplayEmail = (TextView) navHeader.findViewById(R.id.display_email);
        //get navigation menu refs for show/hide menu items when authenticating users
        mNavMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);

        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        prepareFireBaseAuthListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String query = ( (App) getApplicationContext()).getSearchQuery();

        if(!TextUtils.isEmpty(query)) {
            //display search result
            Fragment fragment = MovieListFragment.newInstance(MovieListFragment.DISPLAY_MOVIES_FOR_SEARCH_RESULT, query);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment, MovieListFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            //reset the query search to avoid MainActivity display movie search results when onResume() is called
            ( (App) getApplicationContext()).setSearchQuery("");
        }

    }

    /**
     * Follow android developer guide for launchMode="singleTop"
     * Reference: https://developer.android.com/guide/topics/search/search-dialog.html
     */
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Follow android developer guide for launchMode="singleTop"
     * Reference: https://developer.android.com/guide/topics/search/search-dialog.html
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            ( (App) getApplicationContext()).setSearchQuery(query.trim());

            //save search history
            SearchRecentSuggestions suggestions =
                            new SearchRecentSuggestions(this,
                                                        MovieSearchSuggestionProvider.AUTHORITY,
                                                        MovieSearchSuggestionProvider.MODE);

            suggestions.saveRecentQuery(query, null);
        }
    }

    private void prepareFireBaseAuthListener() {
        //setup listener for authentication changes (when user signin/signout)
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //get authenticated user, if user==null, user is signed out otherwise user is signed in
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null) {
                    //set display name and email in navigation drawer header
                    mHeaderDisplayName.setText(currentUser.getDisplayName());
                    mHeaderDisplayEmail.setText(currentUser.getEmail());

                    //since user is signed in, disable guest mode if it's enabled
                    boolean isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);

                    if(isGuest) {
                        mEditor.putBoolean(GUEST_MODE_PREF_KEY, false);
                        mEditor.apply();
                        //transfer movies from local db to cloud and empty local db
                        TransferLocalMoviesToCloudTask task = new TransferLocalMoviesToCloudTask();
                        task.execute();
                    }
                    
                    //show/hide navigation menu items appropriately
//                    mNavMenu.findItem(R.id.nav_favourites).setVisible(true);
                    mNavMenu.findItem(R.id.auth).getSubMenu().setGroupVisible(R.id.nav_authenticated_group, true);
                    mNavMenu.findItem(R.id.auth).getSubMenu().findItem(R.id.nav_signin).setVisible(false);

                } else {
                    //set display name and email to guest mode since the user is signed out
                    mHeaderDisplayName.setText(R.string.guest);
                    mHeaderDisplayEmail.setText("");

                    //user is signed out, show/hide menus appropriately
//                    mNavMenu.findItem(R.id.nav_favourites).setVisible(false);
                    mNavMenu.findItem(R.id.auth).getSubMenu().setGroupVisible(R.id.nav_authenticated_group, false);
                    mNavMenu.findItem(R.id.auth).getSubMenu().findItem(R.id.nav_signin).setVisible(true);
                    //redirect to home page if the user is not currently on home page
                    if(currentDrawerItemSelected != null && (currentDrawerItemSelected.equals(FavouriteMoviesFragment.FRAGMENT_TAG)
                            || currentDrawerItemSelected.equals(AccountFragment.FRAGMENT_TAG)) ) {
                        //if the current page is favourites or profile page, pop backstack because these
                        //pages are visible to authenticated users only
                        onBackPressed();
                    }
                }
            }
        };
    }

    /**
     * This method helps to manage navigation drawer state, when activity is re-created
     * it won't select the first item to display. Instead, it will display the menu item
     * based on state (before configuration changed)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        //toggle navigation drawer open/close
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        /*
         * Get ViewPager reference, because HomeFragment has been inflated into MainActivity
         * that's why we can get ViewPager reference if it's currently on Home page.
         */
        ViewPager moviePager = (ViewPager) findViewById(R.id.view_pager);

        if(moviePager != null) {
            //Handle back press logic for viewpager here

            //get current position of view pager
            int currentPagerPosition = moviePager.getCurrentItem();
            /**
             * If the user is currently at home page and at the first pager item and only has one left
             * in the backstack (last fragment in the backstack), pop the backstack and destroy activity
             * and exit the app. Otherwise handle back press normally
             *
             * There is another better approach which is store user navigation history in a custom stack
             * such as Stack<Integer>. This way, the user can return to the previous slide (pager) when
             * the back button is pressed. However, there is a "gotcha" with this approach. If the user
             * keep swiping between slides back and forward, the stack will become big and the user has to
             * press the back button numerous times to exit the loop. Solution is to limit the number stored in
             * the custom stack with First-out-Last-in.
             */
            if(currentPagerPosition == 0) {
                if(fm.getBackStackEntryCount() == 1) {
                    super.onBackPressed();
                    finish();
                } else {
                    super.onBackPressed();
                }
            } else if(currentPagerPosition > 0) {
                //set the active pager manually
                moviePager.setCurrentItem(currentPagerPosition - 1);
            }
        } else {
            /**
             * At this stage, the fragment content (HomeFragment) gets destroyed. Therefore, the ViewPager will
             * be null. Handle navigation back the normal way
             */
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        //get search manager
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //get search view
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //set search view config
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        //set listener to listen for searchview text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setIconified(true);
                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    searchView.setIconified(true);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private void clearSearchHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear History");
        builder.setMessage("Are you sure you want to clear your browsing history?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(MainActivity.this,
                        MovieSearchSuggestionProvider.AUTHORITY,
                        MovieSearchSuggestionProvider.MODE);
                suggestions.clearHistory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentManager fm = getSupportFragmentManager();
        String fragmentTag;
        Fragment fragment;

        mDrawerLayout.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                //set flag
                fragmentTag = HomeFragment.FRAGMENT_TAG;
                fragment = fm.findFragmentByTag(fragmentTag);

                if(fragment == null) {
                    fragment = HomeFragment.newInstance();
                    fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                } else {
                    if(!fragment.isAdded()) {
                        //if the fragment is not already added, add it, do nothing otherwise
                        fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                    }
                }

                currentDrawerItemSelected = HomeFragment.FRAGMENT_TAG;
                //set toolbar title
                setToolbarTitle(R.string.title_home);

                return true;

            case R.id.nav_favourites:
                boolean isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);
                FirebaseUser user = mAuth.getCurrentUser();
                if(isGuest || user != null) {
                    //if user is in guest mode, go to favourite page normally
                    fragmentTag = FavouriteMoviesFragment.FRAGMENT_TAG;
                    fragment = fm.findFragmentByTag(fragmentTag);
                    if(fragment == null) {
                        fragment = FavouriteMoviesFragment.newInstance();
                        fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                    } else {
                        if(!fragment.isAdded()) {
                            //if the fragment is not already added, add it, do nothing otherwise
                            fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                        }
                    }
                    currentDrawerItemSelected = FavouriteMoviesFragment.FRAGMENT_TAG;
                    //set toolbar title
                    setToolbarTitle(R.string.title_favourite_movies);

                } else {
                    //redirect to sign in page if user not signed in yet
                    Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                    startActivity(intent);
                }
                return true;

            case R.id.nav_clear_search_history:
                clearSearchHistory();
                return true;

            case R.id.nav_about:
                fragmentTag = AboutUsFragment.FRAGMENT_TAG;
                fragment = fm.findFragmentByTag(fragmentTag);
                if(fragment == null) {
                    fragment = AboutUsFragment.newInstance();
                    fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                } else {
                    if(!fragment.isAdded()) {
                        //if the fragment is not already added, add it, do nothing otherwise
                        fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                    }
                }
                currentDrawerItemSelected = AboutUsFragment.FRAGMENT_TAG;
                //set toolbar title
                setToolbarTitle(R.string.title_about);

                return true;

            case R.id.nav_account:
                fragmentTag = AccountFragment.FRAGMENT_TAG;
                fragment = fm.findFragmentByTag(fragmentTag);
                if(fragment == null) {
                    fragment = AccountFragment.newInstance();
                    fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                } else {
                    if(!fragment.isAdded()) {
                        //if the fragment is not already added, add it, do nothing otherwise
                        fm.beginTransaction().replace(R.id.content_frame, fragment, fragmentTag).addToBackStack(null).commit();
                    }
                }
                currentDrawerItemSelected = AccountFragment.FRAGMENT_TAG;
                //set toolbar title
                setToolbarTitle(R.string.title_about);
                return true;

            case R.id.nav_signin:
                //navigate to signin activity
                Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(intent);
                return true;

            case R.id.nav_signout:
                //sign out user
                signoutUser();
                return true;

            default:
                return true;

        }

    }

    private void signoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout Confirmation");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //check if the user is currently signed in with Facebook
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if(accessToken != null && !accessToken.isExpired()) {
                    //signout Facebook
                    LoginManager.getInstance().logOut();
                }
                //signout Firebase
                mAuth.signOut();
                Utils.createSnackBar(getResources(), findViewById(R.id.root_layout), "You have successfully signed out!").show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();

    }

    private void sendRequestToGetGenreList(String url) {
        JsonObjectRequest genreJsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //successfully get data
                        try {
                            JSONArray genres = response.getJSONArray("genres");
                            parseGenreList(genres);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "onErrorResponse: Error getting genre list " + error.toString());
                    }
                });

        mNetworkRequest.addToRequestQueue(genreJsonRequest, NETWORK_REQUEST_TAG);
    }

    private void parseGenreList(JSONArray genres) {
        for(int i=0; i<genres.length(); i++) {
            try {
                JSONObject genreJsonObj = genres.getJSONObject(i);
                int genreId = genreJsonObj.getInt("id");
                String genreName = genreJsonObj.getString("name");

                Genre genre = new Genre(genreId, genreName);
                mGenreListFromApi.add(genre);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //cache genres list value
        ((App) getApplication()).setGenreListFromApi(mGenreListFromApi);
    }

    private class TransferLocalMoviesToCloudTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<Movie> movieList;

            MoviesTableOperator dbOperator = MoviesTableOperator.getInstance(MainActivity.this);
            //get all movies
            movieList = dbOperator.findAll();
            if(movieList.size() > 0) {
                //successfully got all movies, empty tables
                dbOperator.emptyTables();
                //get user id of current user
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser != null) {

                    //transfer movies from local db to cloud
                    DatabaseReference favouriteMoviesRef = FirebaseDatabase.getInstance().getReference("users")
                            .child(currentUser.getUid())
                            .child("favourite_movies");

                    for(Movie m : movieList) {
                        //since movie id is used as unique key for each movie object on Firebase,
                        //there's no need to check for existing movie, it will overwrite if same movie id found
                        favouriteMoviesRef.child(String.valueOf(m.getId())).setValue(m);
                    }

                }
            }


            return null;

        }
    }

    public void setToolbarTitle(int stringResId) {
        mToolbarTitle.setText(getString(stringResId));
    }

    public void setToolbarTitle(String str) {
        mToolbarTitle.setText(str);
    }

}
