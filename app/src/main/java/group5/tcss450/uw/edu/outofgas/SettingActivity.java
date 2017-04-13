/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Map;

/*
 * Settings activity for changing the search radius and the map type to be used for viewing.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    /*
     * Array of ints for the radius.
     */

    private final int[] radiusArray = {4, 6, 8, 10, 12, 14, 16};

    /*
     * Multipliers for converting the radius to work properly with the map.
     */

    private final int radiusMultiplier = 1000;

    /*
     * Creates the activity and sets up the slider and radio buttons.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setOnClickListener(this);
        }

        group.check(MapsActivity.checkedRadioBtnId);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBarRadius);
        final TextView radius = (TextView) findViewById(R.id.radiusTextView);
        seekBar.setProgress(MapsActivity.radiusProgress);
        radius.setText("Current Radius in Kilometers: " + radiusArray[seekBar.getProgress()]);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radius.setText("Current Radius in Kilometers: " + radiusArray[i]);
                MapsActivity.radiusProgress = i;
                MapsActivity.mRadius = radiusArray[i] * radiusMultiplier;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /*
     * The actions for when a radio button is selected by the user.
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.normalBtn:
                MapsActivity.mMap.setMapType(MapsActivity.mMap.MAP_TYPE_NORMAL);
                MapsActivity.checkedRadioBtnId = R.id.normalBtn;
                break;
            case R.id.satelliteBtn:
                MapsActivity.mMap.setMapType(MapsActivity.mMap.MAP_TYPE_SATELLITE);
                MapsActivity.checkedRadioBtnId = R.id.satelliteBtn;
                break;
            case R.id.hybridBtn:
                MapsActivity.mMap.setMapType(MapsActivity.mMap.MAP_TYPE_HYBRID);
                MapsActivity.checkedRadioBtnId = R.id.hybridBtn;
                break;
        }
    }
}
