/**
 * Loc Bui, Andrew Dinh, Phuc Tran
 * Mar 6, 2017
 * @version: 1.0
 */

package group5.tcss450.uw.edu.outofgas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import group5.tcss450.uw.edu.outofgas.model.DataParser;
import group5.tcss450.uw.edu.outofgas.model.GetNearbyPlacesData;

/*
 * The main map activity that users will see when the log into the app.
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    /*
     * The Google map object.
     */

    public static GoogleMap mMap;

    /*
     * Latitude value for the current location.
     */

    private double latitude;

    /*
     * Longitude value for the current location.
     */

    private double longitude;

    /*
     * The radius for the search of gas stations.
     */

    public static int mRadius = 4000;

    /*
     * Google maps api object.
     */

    private GoogleApiClient mGoogleApiClient;

    /*
     * The current location marker of the user.
     */

    private Marker mCurrLocationMarker;

    /*
     * object for requesting the location.
     */

    private LocationRequest mLocationRequest;

    /*
     * The radio button object.
     */

    public static int checkedRadioBtnId = R.id.normalBtn;

    /*
     * The progress of the slider for the radius.
     */

    public static int radiusProgress = 0;
    /*
     * SharePref for saving user's login
     */
    private SharedPreferences mPrefs;

    /*
     * The boolean value to check if the fragment is at the top of the stack.
     */
    private boolean mCheckFragment = false;

    /*
     * The MenuItem
     */
    private MenuItem mShowEntries;

    /*
     * Creates the activity to appear when user logs in.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mPrefs = getSharedPreferences(getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /*
     * Checks the google play services showing a failure dialog if it fails.
     */

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    /*
     * When the Google maps object is ready, set it to appear.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        Button btnGas = (Button) findViewById(R.id.btnGas);
        btnGas.setOnClickListener(new View.OnClickListener() {
            String gasStation = "gas_station";
            @Override
            public void onClick(View v) {
                mMap.clear();
                String url = getUrl(latitude, longitude, gasStation);
                final Location current = new Location("current");
                current.setLatitude(latitude);
                current.setLongitude(longitude);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                final GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                getNearbyPlacesData.execute(DataTransfer);
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        DataParser dp;
                        dp = getNearbyPlacesData.mList.get(getNearbyPlacesData.mPubMarkerMap.get(marker));
                        Intent intent = new Intent(getApplication(), DetailActivity.class);

                        Location desti = new Location("Destination");
                        desti.setLatitude(Double.parseDouble(dp.getLat()));
                        desti.setLongitude(Double.parseDouble(dp.getLng()));

                        float distanceInMeter = current.distanceTo(desti);
                        double distanceInMiles = getMiles(distanceInMeter);


                        intent.putExtra("name", dp.getName());
                        intent.putExtra("vicinity", dp.getVicinity());
                        intent.putExtra("price", dp.getPriceLevel());
                        intent.putExtra("rating", dp.getRating());
                        intent.putExtra("distance", distanceInMiles);

                        startActivity(intent);
                    }
                });
            }
        });
    }

    /**
     * Helper method to convert meters to miles.
     * @param meters Distance in meter.
     * @return distance in miles
     */
    private double getMiles(float meters) {
        return meters*0.000621371192;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /*
     * Gets the url based on the location.
     */

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + mRadius);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyCfUf81B45d045Yf-9PiCtlF7RXQN9tr7I");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /*
     * When the location changes, update the current location field.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        Location lastLocation = new Location("Last Location");
        lastLocation.setLatitude(latitude);
        lastLocation.setLongitude(longitude);
        if (location.distanceTo(lastLocation) > 1000) {
            //Place current location marker
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LatLng latlng = new LatLng(latitude, longitude);
            Log.d("lat:" , "" + latitude);
            Log.d("lng:" , "" + longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }

    }

    /**
     * This method is used to sign out.
     */

    private void signOut() {
        LoginActivity.user = "";
        VerifyFragment.myVerifyUsername = "";
        checkedRadioBtnId = R.id.normalBtn;
        radiusProgress = 0;
        mRadius = 4000;
        Intent intent = new Intent(getApplication(), LoginActivity.class);
        mPrefs.edit().putString(getString(R.string.username),"0").apply();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /*
     * Signs out the user on the back press.
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mCheckFragment) {
            mPrefs.edit().putString(getString(R.string.username),"0").apply();
            LoginActivity.user = "";
            VerifyFragment.myVerifyUsername = "";
            checkedRadioBtnId = R.id.normalBtn;
            radiusProgress = 0;
            mRadius = 4000;
        } else {
            mCheckFragment = false;
            mShowEntries.setEnabled(true);
        }
        Log.d("Boolean:", mCheckFragment +"");
    }

    /*
     * If the connection fails, prompt the user to allow access to their location.
     */

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Fail:", "Failed to connect");
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /*
     * Requests the user for permission to use location.
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // popup permission requestion for location.
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /*
     * Creates the options menu for the activity.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_in_maps, menu);
        mShowEntries = menu.findItem(R.id.showEntriesInMap);
        return true;
    }

    /*
     * Listener for when a menu item is selected.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.signOut) {
            signOut();
        } else if (id == R.id.setting) {
            Intent intent = new Intent(getApplication(), SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.showEntriesInMap) {
            EntriesFragment entriesFragment = new EntriesFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                    R.anim.enter_from_left, R.anim.exit_to_right);
            transaction.replace(R.id.activity_maps, entriesFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
            fm.executePendingTransactions();
            Log.d("BackStack:", Integer.toString(fm.getBackStackEntryCount()));
            if (fm.getBackStackEntryCount() == 1) {
                mCheckFragment = true;
                mShowEntries.setEnabled(false);
            }
        }
        return true;
    }
}