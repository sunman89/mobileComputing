package com.example.sonny.courseworkindoorlocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private final int CODE_PERMISSIONS = 1;
    private static final String TAG = "MainActivity";
    private TextView mResultText;
    private TextView mSearchtext;
    private Boolean msearchPressed;
    private Button mSearch;
    private Button mStop;
    private IALocationManager mIALocationManager;
    private DatabaseReference mDatabaseRef;
    private IAResourceManager mResourceManager;
    private ImageView mFloorPlanImage;
    private IATask<IAFloorPlan> mPendingAsyncResult;
    private  static final String KEY_INDEX = "searching";

    private IALocation[] mLocations = new IALocation[]{
            new IALocation.Builder().withLatitude(51.52207656090469).withFloorLevel(7).
                    withLongitude(-0.13055164804067468).withLongExtra("Stairs",1).build(),
            new IALocation.Builder().withLatitude(51.52216492953936).withFloorLevel(7).
                    withLongitude(-0.13045009871728871).withLongExtra("Room 736",2).build(),
            new IALocation.Builder().withLatitude(51.521971173070824).withFloorLevel(7).
                    withLongitude(-0.13043352056712815).withLongExtra("Room 724",3).build(),
    };

    private Integer mPoints[] = new Integer[]{
            R.string.stairs,
            R.string.room736,
            R.string.room724,
    };

    private void pointOfInterest(IALocation location){

        Integer mLocFloorLevel = location.getFloorLevel();
        Double mLocLatitude = location.getLatitude();
        Double mLocLongitude = location.getLongitude();
        Location location1 = new Location("Current Location");
        location1.setLatitude(mLocLatitude);
        location1.setLongitude(mLocLongitude);
        Log.d(TAG, "Point of interest Location 1 = " +  location1.toString());
        Integer mPointsLength = mLocations.length;
        Log.d(TAG, "Points length = " + mPointsLength);

        for (int i = 0; i < mPointsLength; i++){
            Log.d(TAG, "Entered for loop");
            // used this if statement here instead of before the loop starts, for if in future more points of interest
            // were added and on different floors
            if(mLocFloorLevel == mLocations[i].getFloorLevel()){
                Location location2 = new Location (getString(mPoints[i]));
                location2.setLatitude(mLocations[i].getLatitude());
                location2.setLongitude(mLocations[i].getLongitude());
                Float mPoint = location1.distanceTo(location2);
                Log.d(TAG, "Location 2 = " + location2.toString());
                Log.d(TAG, "Location 1 distance to location 2 = " + mPoint);

                // Use this or use the haversine forumla, write the website i used to help me, and say i was going to use a
                // method that did the Haversine formula however i saw a comment that mentioned the location.distanceTo method
                // so tried that out and it worked
                // put website used into the labs bookmarks folder
                // https://devdiscoveries.wordpress.com/2010/02/01/android-distance-between-two-points-on-the-earth/
                if(mPoint <= 3){
                    Log.d(TAG, "mPoint is less than or equal to 3 meters");
                    String mToast = getString(mPoints[i]);
                    Toast.makeText(this, mToast, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateDatabase(String mTime,Double mLat,Double mLong, String mCurrentDate, Integer mFloor,
                                Float mAccuracy, Float mCertainty){
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Latitude").setValue(mLat);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Longitude").setValue(mLong);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Floor Level").setValue(mFloor);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Floor Certainty").setValue(mCertainty);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Accuracy").setValue(mAccuracy);
    }

    // Got this code from stackoverflow.com/questions/13241251/timestamp-to-string-date
    public static String getDate(long time){
        DateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = (new Date(time));
        return formatDate.format(currentDate);
    }

    public static String getCurrentTime(long time){

        DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = (new Date(time));
        return formatDate.format(currentTime);
    }

    private IALocationListener mIALocationListener = new IALocationListener() {

        // Called when the location has changed.
        @Override
        public void onLocationChanged(IALocation location) {
            Log.d(TAG, "location changed");
            Log.d(TAG, "Location To string: " + location.toString());

            if(msearchPressed != null && msearchPressed == true){
                Log.d(TAG, "output to result and update database");
                mFloorPlanImage.setVisibility(View.VISIBLE);

                mResultText.setText("\n" + getString(R.string.latitude) + " " +  location.getLatitude() +
                        "\n" + getString(R.string.longitude) + " " + location.getLongitude() +
                        "\n" + getString(R.string.accuracy) + " " + location.getAccuracy() +
                        "\n" + getString(R.string.floorLevel) + " " +  location.getFloorLevel() +
                        " , " + getString(R.string.floorCert) + " " + location.getFloorCertainty() +
                        "\n" + getString(R.string.date) + " " +  getDate(location.getTime()) +
                        " , " + getString(R.string.time) + " " +  getCurrentTime(location.getTime()));

                updateDatabase(getCurrentTime(location.getTime()), location.getLatitude(), location.getLongitude(),
                        getDate(location.getTime()), location.getFloorLevel(), location.getAccuracy(),
                        location.getFloorCertainty());
                pointOfInterest(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged");
            if(status == IALocationManager.STATUS_AVAILABLE)
            {
                String mToast = getString(R.string.statusAvailable);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStatusChanged = " + mToast);
            }
            else if(status == IALocationManager.STATUS_LIMITED)
            {
                String mToast = getString(R.string.statusLimited);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStatusChanged = " + mToast);

            }
            else if(status == IALocationManager.STATUS_OUT_OF_SERVICE){
                String mToast = getString(R.string.statusService);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStatusChanged = " + mToast);

            }
            else if(status == IALocationManager.STATUS_TEMPORARILY_UNAVAILABLE){
                String mToast = getString(R.string.statusUnavailable);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onStatusChanged = " + mToast);

            }
            else if(status == IALocationManager.STATUS_CALIBRATION_CHANGED){
                Log.d(TAG, "onStatusChanged = " + status);
                Log.d(TAG, "calibration is = " + extras);
                if(msearchPressed != null && msearchPressed == true){
                    if(extras.getInt("quality") == IALocationManager.CALIBRATION_POOR)
                    {
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationPoor));
                    }
                    else if(extras.getInt("quality") == IALocationManager.CALIBRATION_GOOD){
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationGood));
                    }
                    else if(extras.getInt("quality") == IALocationManager.CALIBRATION_EXCELLENT){
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationEx));
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultText = (TextView)findViewById(R.id.displayResult);
        mSearchtext = (TextView) findViewById(R.id.searching);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        // Gotten from indoor atlas website
        String[] neededPermissions = {
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        ActivityCompat.requestPermissions( this, neededPermissions, CODE_PERMISSIONS );

        mFloorPlanImage = (ImageView) findViewById(R.id.imageMap);
        mIALocationManager = IALocationManager.create(MainActivity.this);
        mResourceManager = IAResourceManager.create(this);
        Log.d(TAG, "mIALocationManager created");
        Log.d(TAG, "mResourceManager is created");

        if(savedInstanceState != null){
            Log.d(TAG, "saved instance state not null");
            msearchPressed = savedInstanceState.getBoolean(KEY_INDEX,false);
            Log.d(TAG, "msearchPressed = " + msearchPressed);
            if(msearchPressed == true){
                Log.d(TAG, "msearchPressed == true, in saved instance");
                mFloorPlanImage.setVisibility(View.GONE);
                mSearchtext.setText(R.string.searching);
            }
        }

        mSearch = (Button)findViewById(R.id.searchButton);
        mSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "onSearch button pressed");
                mSearchtext.setText(R.string.searching);
                msearchPressed = true;
                onResume();
            }
        });

        mStop = (Button)findViewById(R.id.stopButton);
        mStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "stop button pressed");
                mSearchtext.setText("");
                mResultText.setText(getString(R.string.intro));
                mFloorPlanImage.setVisibility(View.GONE);
                msearchPressed = false;
                onPause();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSavedInstanceState");
        if(msearchPressed != null)
        {
            savedInstanceState.putBoolean(KEY_INDEX, msearchPressed);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(mIALocationManager != null)
        {
            Log.d(TAG, "mIALocation requestLocationUpdates");
            mIALocationManager.requestLocationUpdates(IALocationRequest.create().setFastestInterval(1000), mIALocationListener);
            Log.d(TAG, "mIALocation register region listener");
            mIALocationManager.registerRegionListener(mRegionListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if(mIALocationManager != null)
        {
            Log.d(TAG, "mIALocation removeLocationUpdates");
            mIALocationManager.removeLocationUpdates(mIALocationListener);
            mIALocationManager.unregisterRegionListener(mRegionListener);
            Log.d(TAG, "mIALocation unregister region listener");
        }
    }

    @Override
    protected void onDestroy() {
        if(mIALocationManager != null)
        {
            Log.d(TAG, "mIALocation destroy");
            mIALocationManager.destroy();
        }
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // Gotten from the IndoorAtlas website
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissions");
        for (int i=0; i < grantResults.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                String mToast = getString(R.string.permissions);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(IARegion region) {
            Log.d(TAG, "regionListener onEnterRegion method");
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                fetchFloorPlan(region.getId());
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            Log.d(TAG, "regionlistener onexit region");
            // leaving a previously entered region
        }
    };

    private void fetchFloorPlan(String id) {
        // Cancel pending operation, if any
        Log.d(TAG, "fetchfloor plan method");
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled()) {
            mPendingAsyncResult.cancel();
        }

        mPendingAsyncResult = mResourceManager.fetchFloorPlanWithId(id);
        if (mPendingAsyncResult != null) {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>() {
                @Override
                public void onResult(IAResult<IAFloorPlan> result) {
                    Log.d(TAG, "onResult: %s" + result);

                    if (result.isSuccess()) {
                        handleFloorPlanChange(result.getResult());
                    } else {
                        // do something with error
                        Toast.makeText(MainActivity.this,
                                "loading floor plan failed: " + result.getError(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }

    private void handleFloorPlanChange(IAFloorPlan newFloorPlan) {
        Log.d(TAG, "handle floor plan change method");
        Log.d(TAG, "floor plan info" + newFloorPlan.toString());
        Picasso.with(this)
            .load(newFloorPlan.getUrl())
            .into(mFloorPlanImage);
    }
}