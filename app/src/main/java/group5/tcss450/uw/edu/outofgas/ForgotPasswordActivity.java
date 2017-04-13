/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
 * This is the activity that comes active when the user selects the 
 * forgot password option. It will get their other credentials and send an
 * email to inform them of their password.
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    
    /*
     * Edittext fields for the name, username, and email of the person.
     */

    private EditText fullname, username, email;
    
    /*
     * Partial url for access to the database.
     */

    private static final String PARTIAL_URL
            = "http://cssgate.insttech.washington.edu/" +
            "~locbui/";

    /*
     * Progress bar.
     */
    private ProgressBar mProgressBarForgorPassword;

    /*
     * Forgot password button.
     */
    private Button forgotPwdBtn;
    
    /*
     * Creates the activity and adds click listeners.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mProgressBarForgorPassword = (ProgressBar) findViewById(R.id.progressBarForgotPassword);
        username = (EditText) findViewById(R.id.forgotUsernameText);
        fullname = (EditText) findViewById(R.id.forgotNameText);
        fullname.requestFocus();
        email = (EditText) findViewById(R.id.forgotEmailText);

        forgotPwdBtn = (Button) findViewById(R.id.receivePasswordButton);
        forgotPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTask<String, Void, String> task = null;
                String theUsername = username.getText().toString();
                String theFullName = fullname.getText().toString();
                String theEmail = email.getText().toString();

                task = new GetPasswordTask();
                if (fullname.getText().toString().trim().equalsIgnoreCase("")) {
                    fullname.setError("Please enter your name");
                } else if (username.getText().toString().trim().equalsIgnoreCase("")) {
                    username.setError("Please enter username");
                } else if (email.getText().toString().trim().equalsIgnoreCase("")) {
                    email.setError("Please enter email");
                } else {
                    task.execute(PARTIAL_URL, theFullName, theUsername, theEmail);
                }
            }
        });
    }
    
    /*
     * Web service task that calls the forgotPassword php script.
     */

    private class GetPasswordTask extends AsyncTask<String, Void, String> {
        private final String SERVICE = "forgotPassword.php";

        @Override
        protected void onPreExecute() {
            forgotPwdBtn.setClickable(false);
            mProgressBarForgorPassword.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 4) {
                throw new IllegalArgumentException("Four String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String arg1 = "?full_name=" + strings[1];
            String arg2 = "&user_name=" + strings[2];
            String arg3 = "&email=" + strings[3];
            try {
                URL urlObject = new URL(url + SERVICE + arg1 + arg2 + arg3);
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
                Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();
                return;
            } else if (result.startsWith("Can't find")) {
                Toast toast = Toast.makeText(getApplication(), result, Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            } else {
                String message = "Your password has been sent to your email address!";
                Toast toast = Toast.makeText(getApplication(), message, Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            forgotPwdBtn.setClickable(true);
            mProgressBarForgorPassword.setVisibility(View.GONE);
        }
    }
}