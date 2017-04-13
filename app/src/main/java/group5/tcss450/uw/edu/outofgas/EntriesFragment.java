/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

public class EntriesFragment extends Fragment {
    /**
     * The text view for all saved gas station.
     */
    public static TextView textView;

    public EntriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entries, container, false);
        textView = (TextView) view.findViewById(R.id.entries);
        textView.setMovementMethod(new ScrollingMovementMethod());
        try {
            String fileName = "";
            if (!VerifyFragment.myVerifyUsername.equals("")) {
                fileName = VerifyFragment.myVerifyUsername;
            } else if (!LoginActivity.mUsername.equals("")) {
                fileName = LoginActivity.mUsername;
            } else if (!LoginActivity.user.equals("")) {
                fileName = LoginActivity.user;
            }
            File file = new File(getActivity().getFilesDir(), fileName + ".txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            int ch = 0;
            char c;
            String string = "";
            while ((ch = fileInputStream.read()) != -1) {
                c = (char) ch;
                string = string + c;
            }
            textView.setText(string);
            fileInputStream.close();
        } catch (Exception e) {
            Toast.makeText(this.getContext(), "Nothing to show here", Toast.LENGTH_SHORT).show();
        }
        return view;
    }
}
