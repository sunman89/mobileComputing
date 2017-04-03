package com.example.sonny.courseworkindoorlocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

/**
 * The courseworkIndoorLocation app contains MainActivity that implements Indoor Atlas SDK to make an application that
 * tracks the users location indoors, as long as the location has been mapped using Indoor Atlas map creator.
 * Parts were taken from the Indoor Atlas website, from the get started section, fetch floor plan section, fetch users current location section.
 *
 * @author  Sonny Stokes
 * @version 1.0
 * @since   03/04/2017
 */
public class MainActivity extends AppCompatActivity
{
    // All the variables used throughout the app
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

    // An array of IALocations, that are used as points of interest
    private IALocation[] mLocations = new IALocation[]
    {
        new IALocation.Builder().withLatitude(51.52207656090469).withFloorLevel(7).
            withLongitude(-0.13055164804067468).withLongExtra("Stairs",1).build(),
        new IALocation.Builder().withLatitude(51.52216492953936).withFloorLevel(7).
            withLongitude(-0.13045009871728871).withLongExtra("Room 736",2).build(),
        new IALocation.Builder().withLatitude(51.521971173070824).withFloorLevel(7).
            withLongitude(-0.13043352056712815).withLongExtra("Room 724",3).build(),
    };

    // An array of string references, that contain strings messages for each point of interest
    private Integer mPoints[] = new Integer[]
    {
        R.string.stairs,
        R.string.room736,
        R.string.room724,
    };

    /**
     * This method is used to check if the user is within 3 meters from a point of interest.
     * It gets the users current IAlocation, then it stores that as a location1. It then enters a for loop which loops through the points of interest array,
     * then checks if the user is on the same floor as the current point of interest. If the user is on the same floor, it then creates a new location2 to store the current
     * point of interests latitude and longitude. It does this to be able to call the method distanceto() on the current location1 and use location2 as a parameter.
     * It stores the distance to point of interest from current location into mPoint. It then checks if mPoint is less than 3, if true it will show a toast to the user
     * that contains a message related to that current point of interest.
     * @param location This is current location of the user
     * @return void
     */
    private void pointOfInterest(IALocation location)
    {
        Integer mLocFloorLevel = location.getFloorLevel();
        Double mLocLatitude = location.getLatitude();
        Double mLocLongitude = location.getLongitude();
        Location location1 = new Location("Current Location");
        location1.setLatitude(mLocLatitude);
        location1.setLongitude(mLocLongitude);
        Integer mPointsLength = mLocations.length;

        for (int i = 0; i < mPointsLength; i++)
        {
            // used this if statement here instead of before the loop starts, for if in future more points of interest were added and on different floors
            if(mLocFloorLevel == mLocations[i].getFloorLevel())
            {
                Location location2 = new Location (getString(mPoints[i]));
                location2.setLatitude(mLocations[i].getLatitude());
                location2.setLongitude(mLocations[i].getLongitude());
                /**
                 * Reference = https://devdiscoveries.wordpress.com/2010/02/01/android-distance-between-two-points-on-the-earth/
                 * Was planning to use the Haversine formula from this web page, but saw a comment on that page, that recommended using the location.distanceto() method instead.
                 * So i decided it would be more efficient to create location objects, give them the latitudes and longitudes of the current location and the points of interest
                 * and use the distanceto() method. Plus it returns the distance in meters
                 */
                Float mPoint = location1.distanceTo(location2);

                // Check if the distance between the two locations is less than 3 meters
                if(mPoint < 3)
                {
                    String mToast = getString(mPoints[i]);
                    Toast.makeText(this, mToast, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * This method is used to send the users current location data to the realtime database on Firebase.
     * It uses the database reference to the Firebase realtime database, to store the data. It stores the date first, so it keeps a collection of what the user did on each day,
     * it then stores the current time next, then finally stores the location accuracy, floor certainty, floor level, longitude and latitude.
     * @param mTime is the current time of the location change
     * @param mLat is the current location latitude
     * @param mLong is the current location longitude
     * @param mCurrentDate is the current date
     * @param mFloor is the current location floor level
     * @param mAccuracy is the current location accuracy
     * @param mCertainty is the current location floor certainty
     * @return void
     */
    private void updateDatabase(String mTime,Double mLat,Double mLong, String mCurrentDate, Integer mFloor, Float mAccuracy, Float mCertainty)
    {
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Latitude").setValue(mLat);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Longitude").setValue(mLong);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Floor Level").setValue(mFloor);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Floor Certainty").setValue(mCertainty);
        mDatabaseRef.child("tracking").child(mCurrentDate).child(mTime).child("Accuracy").setValue(mAccuracy);
    }

    /**
     * This method is used to get the current date from a timestamp
     * It takes in a timestamp and converts it into just the date with format dd-MM-yyyy.
     * I got this from stackoverflow.com/questions/13241251/timestamp-to-string-date
     * @param time This is current time as a timestamp
     * @return getDate , which is the current timestamp that has been converted into the current date
     */
    public static String getDate(long time)
    {
        DateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = (new Date(time));
        return formatDate.format(currentDate);
    }

    /**
     * This method is used to get the current time from a timestamp
     * It takes in a timestamp and converts it into just the time with format HH:mm:ss.
     * @param time This is current time as a timestamp
     * @return getCurrentTime , which is the current timestamp that has been converted into the current time
     */
    public static String getCurrentTime(long time)
    {
        DateFormat formatDate = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = (new Date(time));
        return formatDate.format(currentTime);
    }

    // This is a variable created to listen out for a change in users location, got from Indoor Atlas website
    private IALocationListener mIALocationListener = new IALocationListener()
    {
        /**
         * This method is used when the users location has changed.
         * Got the method call from Indoor Atlas website.
         * It takes in the users current location every second, it checks whether or not the user has pressed the search button, if not then it wont do anything else.
         * If the search button has been pressed, then it will change the imageview to be displayed, it will then set the text of the results textview to show the users current
         * latitude, longitude, accuracy, floor level, floor certainty, date and time. It will then call the updateDatabase() method to update the Firebase realtime database, with the
         * correct values.
         * It will then call the pointOfInterest() method, and send the current location to it.
         * @param location This is current location of the user
         * @return void
         */
        // Called when the location has changed.
        @Override
        public void onLocationChanged(IALocation location)
        {

            if(msearchPressed != null && msearchPressed == true)
            {
                mFloorPlanImage.setVisibility(View.VISIBLE);

                mResultText.setText("\n" + getString(R.string.latitude) + " " +  location.getLatitude() +
                        "\n" + getString(R.string.longitude) + " " + location.getLongitude() +
                        "\n" + getString(R.string.accuracy) + " " + location.getAccuracy() +
                        "\n" + getString(R.string.floorLevel) + " " +  location.getFloorLevel() +
                        " , " + getString(R.string.floorCert) + " " + location.getFloorCertainty() +
                        "\n" + getString(R.string.date) + " " +  getDate(location.getTime()) +
                        " , " + getString(R.string.time) + " " +  getCurrentTime(location.getTime()));

                updateDatabase(getCurrentTime(location.getTime()), location.getLatitude(), location.getLongitude(),
                        getDate(location.getTime()), location.getFloorLevel(), location.getAccuracy(), location.getFloorCertainty());

                pointOfInterest(location);
            }
        }

        /**
         * This method is used to check if the status of the network/wifi/calibration has changed
         * Got this method call from Indoor Atlas website.
         * It checks if the status has changed for the wifi, network, phone calibration and then it displays a toast or message to the user telling them of this change.
         * It checks if the status is available and can start getting location updates.
         * It checks if the status is limited and can maybe get location updates.
         * It checks if the status is out of service, therefore can get no location updates.
         * It checks if the status is just temporarily out of service and will eventually come back.
         * It checks if the calibration has changed, and it updates the user telling them what it currently is.
         * @param provider is the provider
         * @param status is the current status
         * @param extras is the extras, which contain the calibration change quality
         * @return void
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            if(status == IALocationManager.STATUS_AVAILABLE)
            {
                String mToast = getString(R.string.statusAvailable);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
            else if(status == IALocationManager.STATUS_LIMITED)
            {
                String mToast = getString(R.string.statusLimited);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
            else if(status == IALocationManager.STATUS_OUT_OF_SERVICE)
            {
                String mToast = getString(R.string.statusService);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
            else if(status == IALocationManager.STATUS_TEMPORARILY_UNAVAILABLE)
            {
                String mToast = getString(R.string.statusUnavailable);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
            else if(status == IALocationManager.STATUS_CALIBRATION_CHANGED)
            {
                if(msearchPressed != null && msearchPressed == true)
                {
                    if(extras.getInt("quality") == IALocationManager.CALIBRATION_POOR)
                    {
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationPoor));
                    }
                    else if(extras.getInt("quality") == IALocationManager.CALIBRATION_GOOD)
                    {
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationGood));
                    }
                    else if(extras.getInt("quality") == IALocationManager.CALIBRATION_EXCELLENT)
                    {
                        mSearchtext.setText(getString(R.string.searching) + "\n" + getString(R.string.calibrationEx));
                    }
                }
            }
        }
    };

    /**
     * This method is used to create the ui and the various components of the app
     * It takes savedInstanceState as a parameter, to check the state of the app, whether it is searching for a location or not. Mainly used when switching from landscape to portrait.
     * @param savedInstanceState This is current state of the app
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultText = (TextView)findViewById(R.id.displayResult);
        mSearchtext = (TextView) findViewById(R.id.searching);
        // Gets a reference to the Firabase realtime database
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

        /**
         * Checks the saved instance state, if not null it will save the value of whether the search button was pressed and the app was searching for a location, if false, it will
         * make the app start as if it was anew, if it is true, then it will set all the text views to display it is searching and display the imageview.
         */
        if(savedInstanceState != null)
        {
            msearchPressed = savedInstanceState.getBoolean(KEY_INDEX,false);
            if(msearchPressed == true)
            {
                mFloorPlanImage.setVisibility(View.GONE);
                mSearchtext.setText(R.string.searching);
            }
        }

        mSearch = (Button)findViewById(R.id.searchButton);
        mSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSearchtext.setText(R.string.searching);
                msearchPressed = true;
                onResume();
            }
        });

        mStop = (Button)findViewById(R.id.stopButton);
        mStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSearchtext.setText("");
                mResultText.setText(getString(R.string.intro));
                mFloorPlanImage.setVisibility(View.GONE);
                msearchPressed = false;
                onPause();
            }
        });
    }

    /**
     * This method is used to set key values when the app changes state
     * It saves the value of msearchPressed, which if true, the user was searching for a location. If false, the user has not pressed the search button and is not currently tracking their
     * location.
     * @param savedInstanceState This is current savedInstanceState
     * @return void
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        if(msearchPressed != null)
        {
            savedInstanceState.putBoolean(KEY_INDEX, msearchPressed);
        }
    }

    /**
     * This method is used when the app resumes
     * It checks if the location manager is not null, if it is not null, then it requests location updates every second, and it registers the region listener.
     * @return void
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        if(mIALocationManager != null)
        {
            mIALocationManager.requestLocationUpdates(IALocationRequest.create().setFastestInterval(1000), mIALocationListener);
            mIALocationManager.registerRegionListener(mRegionListener);
        }
    }

    /**
     * This method is used when the app is paused
     * It checks whether the location manager is not null, if it is not null, then it remove the location updates, to stop receiving updates, and unregisters the region listener.
     * @return void
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        if(mIALocationManager != null)
        {
            mIALocationManager.removeLocationUpdates(mIALocationListener);
            mIALocationManager.unregisterRegionListener(mRegionListener);
        }
    }

    /**
     * This method is used when the app is fully closed and the data created is destroyed
     * It checks if the location manager is not null, if it is not null, it will destroy the location manager and then destroy the apps oncreated data.
     * @return void
     */
    @Override
    protected void onDestroy()
    {
        if(mIALocationManager != null)
        {
            mIALocationManager.destroy();
        }
        super.onDestroy();
    }

    /**
     * This method is used to check if the user has given the app the permissions it needs to work.
     * Got method call from the IndoorAtlas website
     * It checks each permission and if the user has granted the app the permission to use that permission.
     * It checks if one of the permissions has not been granted, if so, it will display a toast to the user telling them it needs to use those permissions to work.
     * @param requestCode the code specified to get a result, a key
     * @param permissions each permission in an array
     * @param grantResults an array of each permission grant result
     * @return void
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i=0; i < grantResults.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                String mToast = getString(R.string.permissions);
                Toast.makeText(MainActivity.this, mToast, Toast.LENGTH_SHORT).show();
            }
        }

    }

    // This is a variable which registers a region listener
    private IARegion.Listener mRegionListener = new IARegion.Listener()
    {

        /**
         * This method is used to check if the user has entered a region
         * Got method call from the IndoorAtlas website
         * It checks if the user has entered a region, it gets that region and it then uses the method fetchFloorPlan() to fetch the floor plan for the users location based on the
         * regions ID.
         * @param region the current region of the user
         * @return void
         */
        @Override
        public void onEnterRegion(IARegion region)
        {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN)
            {
                fetchFloorPlan(region.getId());
            }
        }

        /**
         * This method is used to check if the user has left the current region
         * Got method call from the IndoorAtlas website
         * It checks if the user has exited the region
         * @param region the region the user is currently leaving
         * @return void
         */
        @Override
        public void onExitRegion(IARegion region)
        {
            // leaving a previously entered region
        }
    };

    /**
     * This method is used to fetch the floor plan to display it to user or add onto a map via ground overlay
     * Gotten from Indoor Atlas website
     * It uses the region id, to get a floor plan object which can then be displayed onto an imageview or a map.
     * @param id This is current region id
     * @return void
     */
    private void fetchFloorPlan(String id)
    {
        // Cancel pending operation, if any
        if (mPendingAsyncResult != null && !mPendingAsyncResult.isCancelled())
        {
            mPendingAsyncResult.cancel();
        }

        mPendingAsyncResult = mResourceManager.fetchFloorPlanWithId(id);
        if (mPendingAsyncResult != null)
        {
            mPendingAsyncResult.setCallback(new IAResultCallback<IAFloorPlan>()
            {
                @Override
                public void onResult(IAResult<IAFloorPlan> result)
                {

                    if (result.isSuccess())
                    {
                        handleFloorPlanChange(result.getResult());
                    }
                    else
                    {
                        // do something with error
                        Toast.makeText(MainActivity.this,
                                "loading floor plan failed: " + result.getError(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }, Looper.getMainLooper()); // deliver callbacks in main thread
        }
    }

    /**
     * This method is used to handle the floor plan change
     * It takes in a floor plan object, and it uses picasso to get the floor plan image by using the floor plan URL. It then inserts that floor plan image into the imageview to be
     * displayed to the user.
     * @param newFloorPlan This is current floor plan object
     * @return void
     */
    private void handleFloorPlanChange(IAFloorPlan newFloorPlan)
    {
        Picasso.with(this)
            .load(newFloorPlan.getUrl())
            .into(mFloorPlanImage);
    }
}