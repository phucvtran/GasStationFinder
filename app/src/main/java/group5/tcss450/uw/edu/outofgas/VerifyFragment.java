/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Fragment for verifying the user registration.
 */

public class VerifyFragment extends Fragment {

    /*
     * The username that is used to pass to comment activity.
     */

    public static String myVerifyUsername = "";

    /*
     * Edittext field for the verifcation code.
     */

    private EditText verifyCode;

    /*
     * Edittext field for the username of the account to be verified.
     */

    private EditText verifyUsername;
    
    /*
     * Partial url for access to the database.
     */

    private static final String PARTIAL_URL
            = "http://cssgate.insttech.washington.edu/" +
            "~locbui/";

    /*
    * Progress bar.
    */
    private ProgressBar mProgressBarVerify;

    /*
     * Verify account button.
     */
    private Button verifyAccount;

    public VerifyFragment() {
        // Required empty public constructor
    }

    /*
     * Creates the fragment and sets the click listeners.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verify, container, false);

        mProgressBarVerify = (ProgressBar) view.findViewById(R.id.progressBarVerify);

        verifyCode = (EditText) view.findViewById(R.id.verifyCodeText);
        verifyUsername = (EditText) view.findViewById(R.id.usernameVerify);

        verifyAccount = (Button) view.findViewById(R.id.verifyButton);
        verifyAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AsyncTask<String, Void, String> task;
                String verifyCodeString = verifyCode.getText().toString();
                String verifyUsernameString = verifyUsername.getText().toString();

                task = new GetWebServiceTaskRegister();
                if (verifyCode.getText().toString().equalsIgnoreCase("")) {
                    verifyCode.setError("Please enter your verify code");
                } else if (verifyUsername.getText().toString().equalsIgnoreCase("")) {
                    verifyUsername.setError("Please enter your username");
                } else if (verifyUsername.getText().toString().contains("'")) {
                    verifyUsername.setError("Username cannot contain special character");
                } else {
                    myVerifyUsername = verifyUsernameString;
                    task.execute(PARTIAL_URL, verifyCodeString, verifyUsernameString);
                }
            }
        });

        return view;
    }
    
    /*
     * Web service task that calls the registerPermanent php script.
     */

    private class GetWebServiceTaskRegister extends AsyncTask<String, Void, String> {
        private final String SERVICE = "registerPermanent.php";

        @Override
        protected void onPreExecute() {
            verifyAccount.setClickable(false);
            mProgressBarVerify.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 3) {
                throw new IllegalArgumentException("Three String arguments required.");
            }
            String response = "";
            HttpURLConnection urlConnection = null;
            String url = strings[0];
            String arg0 = "?verify=" + strings[1];
            String arg1 = "&username=" + strings[2];
            try {
                URL urlObject = new URL(url + SERVICE + arg0 + arg1);
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
                Toast.makeText(getActivity(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            } else if (!result.isEmpty()) {
                Toast.makeText(getActivity(), "Welcome!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            } else {
                String message = "Invalid code or you account has been registered already!";
                Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }

            verifyAccount.setClickable(true);
            mProgressBarVerify.setVisibility(View.GONE);
        }
    }
}