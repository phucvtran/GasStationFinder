/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/*
 * This is the activity that comes active when the user wants to
 * register for the application.
 */

public class RegisterActivity extends AppCompatActivity {
    
     /*
     * Edittext fields for the name, username, password, the confirmation password, 
     * and email of the person.
     */

    private EditText fullname, username, password, passwordConfirm, email;
    
    /*
     * The string for the verify code.
     */

    private String verifyCodeStr;
    
    /*
     * The integer of the verify code.
     */

    private int verifyCode;
    
    /*
     * Partial url for access to the database.
     */

    private static final String PARTIAL_URL
            = "http://cssgate.insttech.washington.edu/" +
            "~locbui/";

    /*
    * Progress bar.
    */
    private ProgressBar mProgressBarRegister;

    /*
     * Register button.
     */
    private Button registerButton;
    
    /*
     * Creates the register activity and sets the click listeners.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressBarRegister = (ProgressBar) findViewById(R.id.progressBarRegister);

        fullname = (EditText) findViewById(R.id.fullNameText);
        fullname.requestFocus();
        username = (EditText) findViewById(R.id.usernameReg);
        password = (EditText) findViewById(R.id.passwordReg);
        passwordConfirm = (EditText) findViewById(R.id.confirmPasswordReg);
        email = (EditText) findViewById(R.id.emailReg);
        Random r = new Random();
        int max = 999999;
        int min = 100000;
        verifyCode = r.nextInt(max - min + 1) + min;

        final CheckBox showPassword = (CheckBox) findViewById(R.id.showPasswordCheckBox);
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPassword.isChecked()) {
                    password.setTransformationMethod(null);
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        registerButton = (Button) findViewById(R.id.createAccountButton);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AsyncTask<String, Void, String> task = null;
                AsyncTask<String, Void, String> sendEmailTask = null;
                String theFullName = fullname.getText().toString();
                String theUsername = username.getText().toString();
                String thePassword = password.getText().toString();
                String theEmail = email.getText().toString();
                verifyCodeStr = Integer.toString(verifyCode);
                task = new GetWebServiceTaskRegister();
                sendEmailTask = new SendEmailWebService();
                if (fullname.getText().toString().trim().equalsIgnoreCase("")) {
                    fullname.setError("Please enter your name");
                } else if (username.getText().toString().trim().equalsIgnoreCase("") || username.getText().toString().length() < 4) {
                    username.setError("Please enter username with at least 4 characters");
                } else if (password.getText().toString().trim().equalsIgnoreCase("") || password.getText().toString().length() < 4) {
                    password.setError("Please enter password with at least 4 characters");
                } else if (!(password.getText().toString().trim().equals(passwordConfirm.getText().toString().trim()))) {
                    passwordConfirm.setError("Password does not match!");
                } else if (email.getText().toString().trim().equalsIgnoreCase("")) {
                    email.setError("Please enter email");
                } else if (username.getText().toString().contains("'")) {
                    username.setError("Username cannot contain special character");
                } else if (password.getText().toString().contains("'")) {
                    password.setError("Password cannot contain special character");
                } else {
                    task.execute(PARTIAL_URL, theFullName, theUsername, thePassword, theEmail, verifyCodeStr);
                    sendEmailTask.execute(PARTIAL_URL, theEmail, verifyCodeStr, theUsername);
                }
            }
        });
    }
    
    /*
     * Web service task that calls the registerApp php script.
     */

    private class GetWebServiceTaskRegister extends AsyncTask<String, Void, String> {
        private final String SERVICE = "registerApp.php";

        @Override
        protected void onPreExecute() {
            registerButton.setClickable(false);
            mProgressBarRegister.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 6) {
                throw new IllegalArgumentException("Six String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String arg0 = "?full_name=" + strings[1];
            String arg1 = "&user_name=" + strings[2];
            String arg2 = "&pass_word=" + strings[3];
            String arg3 = "&email=" + strings[4];
            String arg4 = "&verify=" + strings[5];
            try {
                URL urlObject = new URL(url + SERVICE + arg0 + arg1 + arg2 + arg3 + arg4);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
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
            } else if (!result.isEmpty() && result.startsWith("Registered Success")) {
                Toast.makeText(getApplication(), "Register Success!", Toast.LENGTH_LONG).show();
                changeFragment();
            } else {
                Toast.makeText(getApplication(), "Register Failed! Username already exists!", Toast.LENGTH_LONG).show();
            }
            registerButton.setClickable(true);
            mProgressBarRegister.setVisibility(View.GONE);
        }
    }
    
    /*
     * Web service task that calls the sendEmail php script.
     */

    private class SendEmailWebService extends AsyncTask<String, Void, String> {
        private final String SERVICE = "sendEmail.php";
        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 4) {
                throw new IllegalArgumentException("Four String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String arg0 = "?email=" + strings[1];
            String arg1 = "&code=" + strings[2];
            String arg2 = "&user_name=" + strings[3];
            try {
                URL urlObject = new URL(url + SERVICE + arg0 + arg1 + arg2);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
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
            }
        }
    }
    
    /*
     * Changes the fragment that is currently in view.
     */

    private void changeFragment() {
        VerifyFragment verifyFragment = new VerifyFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_register, verifyFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}