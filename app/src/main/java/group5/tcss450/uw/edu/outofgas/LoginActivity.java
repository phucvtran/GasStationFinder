/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * This is the activity that runs the log in activity for the user.
 * It checks the entered username and password to see if there is a 
 * verifies user by these credentials. This is also the activity where
 * they may press the register or forgot password buttons to go to their
 * related activities.
 */

public class LoginActivity extends AppCompatActivity {

    /*
     * The static username used to pass to another activity;
     */
    public static String user = "";
    
    /*
     * Edittext fields that hold the entered username and password.
     */
    private EditText username, password;

    /*
     * Partial url for access to the database.
     */
    private static final String PARTIAL_URL
            = "http://cssgate.insttech.washington.edu/" +
            "~locbui/";

    /*
     * SharePref for saving user's login
     */
    private SharedPreferences mPrefs;

    /*
     * The boolean value for the check box.
     */
    private boolean mCheckBox;

    /*
     * Check variable for save log in.
     */
    private boolean mLogin;

    /*
     * value to store the username
     */
    public static String mUsername = "";

    /*
     * Login button
     */
    private Button loginButton;

    /*
    * Register button
    */
    private Button registerButton;

    /*
    * Progress bar for logging in
    */
    private ProgressBar mProgressBarLogin;

    /*
     * Creates the activity and adds click listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = getSharedPreferences(getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
        mUsername = mPrefs.getString(getString(R.string.username),"0");
        if (!mUsername.equals("0")) {
            Intent intent = new Intent(getApplication(), MapsActivity.class);
            startActivity(intent);
            Log.d("username", mUsername);
        }
        username = (EditText) findViewById(R.id.username);
        username.requestFocus();
        password = (EditText) findViewById(R.id.password);

        registerButton = (Button) findViewById(R.id.registerButton);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTask<String, Void, String> task;
                String theUsername = username.getText().toString();
                String thePassword = password.getText().toString();

                task = new GetWebServiceTask();
                if (username.getText().toString().trim().equalsIgnoreCase("")) {
                    username.setError("Please enter username");
                } else if (password.getText().toString().trim().equalsIgnoreCase("")) {
                    password.setError("Please enter password");
                } else if (username.getText().toString().contains("'")) {
                    username.setError("Username cannot contain special character");
                } else if (password.getText().toString().contains("'")) {
                    password.setError("Password cannot contain special character");
                } else {
                    mUsername = theUsername;
                    user = theUsername;
                    task.execute(PARTIAL_URL, theUsername, thePassword);
                }
            }
        });

        mProgressBarLogin = (ProgressBar) findViewById(R.id.progressBarLogin);
        mProgressBarLogin.setVisibility(View.INVISIBLE);
    }

    /*
     * Check to see if the user choose to remember their login
     */
    private boolean isCheck(){
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        if(checkBox.isChecked()) { return mCheckBox = true;}
        return mCheckBox;
    }

    /*
     * Store the username to sharedPref
     */
    public void saveToSharePref(String theUsername){
        if (isCheck()&& mLogin) {
            mPrefs.edit().putString(getString(R.string.username),theUsername).apply();
        }
    }

    /*
     * Web service task that calls the loginapp php script.
     * This checks if the user exists in the system and if they entered
     * the correct password.
     */

    private class GetWebServiceTask extends AsyncTask<String, Void, String> {
        private final String SERVICE = "loginApp.php";

        @Override
        protected void onPreExecute() {
            loginButton.setClickable(false);
            registerButton.setClickable(false);
            mProgressBarLogin.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 3) {
                throw new IllegalArgumentException("Two String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String arg1 = "?user_name=" + strings[1];
            String arg2 = "&pass_word=" + strings[2];

            try {
                URL urlObject = new URL(url + SERVICE + arg1 + arg2);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            } catch (Exception e) {
                response = "Unable to connect, Reason: "
                        + e.getMessage();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            if (result.startsWith("Unable to")) {
                Toast.makeText(getApplication(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            } else if (!result.isEmpty() && !result.startsWith("Unable to")) {
                Toast.makeText(getApplication(), "Login Success!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplication(), MapsActivity.class);
                mLogin = true;
                saveToSharePref(mUsername);
                startActivity(intent);
            } else if (result.isEmpty() && !result.startsWith("Unable to")) {
                Toast toast = Toast.makeText(getApplication(), "Login Failed! Invalid Username or Password", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
            loginButton.setClickable(true);
            registerButton.setClickable(true);
            mProgressBarLogin.setVisibility(View.GONE);
        }
    }

    /*
     * If the user presses the register buttons, this will take them to the
     * register activity.
     */

    public void registerMethod(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /*
     * If the user forgets their password, this will take them to the
     * forgot password activity.
     */

    public void forgotPasswordMethod(View v) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    /**
     * If the user already created an account but did not verify it yet.
     * This method will take them to the verify fragment.
     * @param v View
     */
    public void verifyAccount(View v) {
        VerifyFragment verifyFragment = new VerifyFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_login, verifyFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}
