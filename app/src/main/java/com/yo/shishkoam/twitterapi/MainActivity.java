package com.yo.shishkoam.twitterapi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.UserTimeline;
import com.yo.shishkoam.twitterapi.twitter.NewTweetDialogFragment;
import com.yo.shishkoam.twitterapi.twitter.OnAuthorClickCallback;
import com.yo.shishkoam.twitterapi.twitter.TwitAdapter;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements Const {

    private ListView listView;
    private TwitterLoginButton loginButton;
    private TwitterSession session;
    private final static String TWITTER_KEY = "V9aCPI2rbZdWU9XiTTKpT4nyf";
    private final static String TWITTER_SECRET = "dFHF1WWw3goP7zfqghbnwylpEa7YGjGb2vrAickc2NB9hiZZAu";
    private String searchText;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWriteTwitDialog();
            }
        });

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        boolean isOpen = openTwiSession();
        if (isOpen) {
            loginButton.setVisibility(View.GONE);
        } else {
            initLoginButton();
        }

        listView = (ListView) findViewById(R.id.list);
        if (UserManager.getInstance().isEmpty()) {
            initListView(DEFAULT_USER, onAuthorClickCallback);
        } else {
            initListView(UserManager.getInstance().getLastUser(), onAuthorClickCallback);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initListView(UserManager.getInstance().getLastUser(), onAuthorClickCallback);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    //saving search string if opened
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH, searchText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchText = savedInstanceState.getString(SEARCH, null);
        if (searchText != null) {
            showFindUserDialog();
        }
    }

    //work with session
    private void initLoginButton() {
        loginButton.setVisibility(View.VISIBLE);
        loginButton.setEnabled(true);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = result.data;
                saveTwitterSession(session);
                loginButton.setVisibility(View.GONE);
                String msg = session.getUserName() + getString(R.string.logged_in);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private boolean openTwiSession() {
        session = Twitter.getSessionManager().getActiveSession();
        if (session == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long userID = prefs.getLong(USER_ID, -1);
            if (userID != -1) {
                String token = prefs.getString(TOKEN, "");
                String secret = prefs.getString(SECRET, "");
                String userName = prefs.getString(USER_NAME, "");
                SessionManager sessionManager = Twitter.getSessionManager();
                TwitterAuthToken authToken = new TwitterAuthToken(token, secret);
                session = new TwitterSession(authToken, userID, userName);
                sessionManager.setActiveSession(session);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void saveTwitterSession(TwitterSession session) {
        TwitterAuthToken authToken = session.getAuthToken();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String token = authToken.token;
        String secret = authToken.secret;
        String userName = session.getUserName();
        long userID = session.getUserId();
        editor.putString(TOKEN, token);
        editor.putString(SECRET, secret);
        editor.putString(USER_NAME, userName);
        editor.putLong(USER_ID, userID);
        editor.commit();
    }

    //show user tweet timeline
    private void initListView(final String user, final OnAuthorClickCallback onAuthorClickCallback) {
        final UserTimeline userTimeline = new UserTimeline.Builder().screenName(user).build();
        final TwitAdapter adapter = new TwitAdapter(this, userTimeline);
        adapter.setOnAuthorClickCallback(onAuthorClickCallback);
        listView.setAdapter(adapter);
        //saving user to history
        UserManager.getInstance().addUser(user);
    }

    private OnAuthorClickCallback onAuthorClickCallback = new OnAuthorClickCallback() {
        @Override
        public void onAuthorClick(String author) {
            initListView(author, this);
        }
    };

    @Override
    public void onBackPressed() {
        //going back to previous user if we have one
        UserManager.getInstance().removeLastUser();
        if (UserManager.getInstance().isEmpty()) {
            super.onBackPressed();
        } else {
            initListView(UserManager.getInstance().getLastUser(), onAuthorClickCallback);
        }
    }

    //writing new tweet methods
    private void showWriteTwitDialog() {
        if (session == null) {
            loginButton.performClick();
            return;
        }
        new NewTweetDialogFragment().show(getSupportFragmentManager(), "tag");
    }

    //menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutFromTwitter();
            return true;
        } else if (id == R.id.go_to_user) {
            showFindUserDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutFromTwitter() {
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().clear().commit();
        initLoginButton();
    }

    private void showFindUserDialog() {
        final EditText findEditText = new EditText(this);
        findEditText.setHint(DEFAULT_USER);
        //this part for saving instance when rotation
        if (searchText != null) {
            findEditText.setText(searchText);
        }
        findEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(findEditText)
                .setPositiveButton(R.string.find_user, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initListView(findEditText.getText().toString(), onAuthorClickCallback);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        searchText = null;
                    }
                })
                .setTitle(R.string.user_search);
        builder.show();
    }
}
