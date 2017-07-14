package nxp.muiticastdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class SmartConfigActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private boolean mIsConfiguring = false;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView mRcvView;
    private Button mEmailSignInButton;
    private View mProgressView;
    private SmartConfigSocket mSmartConfigSocket;
    protected BroadcastReceiver mBroadcastReceiver;
    ArrayList<String> mMsgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartconfig);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.SSID);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo == null || activeNetInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            mEmailView.setText(getString(R.string.error_no_wifi));
            mEmailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorError));
            mEmailView.setError(getString(R.string.error_no_wifi));
        } else {
            WifiManager mWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifi.getConnectionInfo();

            mEmailView.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mRcvView = (TextView) findViewById(R.id.recive);
        mMsgList = new ArrayList<String>();

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mSmartConfigSocket = new SmartConfigSocket(this);

        mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mIsConfiguring) {
                        String text = intent.getExtras().getString("key") + "\n";
                        boolean in = false;

                        for (String str : mMsgList)
                            if (str.equals(text)) {
                                in = true;
                                break;
                            }

                        if (!in) {
                            mMsgList.add(text);
                            mRcvView.append(text);
                        }
                    }
                }
            };

        this.registerReceiver(mBroadcastReceiver,
                new IntentFilter("android.intent.action.NEWDEVICE"));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mIsConfiguring) {
            mEmailSignInButton.setText(R.string.action_sign_in);
            showProgress(false);
            mIsConfiguring = false;
            mSmartConfigSocket.stopSendPacket();
            return;
        }

        // Reset errors.
        mPasswordView.setError(null);
        mRcvView.setText(null);
        mMsgList.clear();

        // Store values at the time of the login attempt.
        String ssid = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (mEmailView.getError() != null){
            return;
        } else if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mEmailSignInButton.setText(R.string.action_cancel);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            showProgress(true);
            mIsConfiguring = true;

            mSmartConfigSocket.startSendPacket(ssid, password);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 1;
    }

    /**
     * Shows the progress UI and hides the login form.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }
}