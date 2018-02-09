package com.fsprite.aarkh.locatorjobservice;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestServiceActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LocatorJobServiceLogs";
    private static final int JOB_ID = getSomeRandomNumber();
    private static final int TEST_PERMISSION_REQUEST = getSomeRandomNumber();

    ComponentName mTestedService;
    JobInfo mJobInfo;
    JobScheduler mScheduler;

    Button mStartButton;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_test_service );
        mStartButton = (Button) findViewById( R.id.startButton );
    }

    @Override
    protected void onResume() {
        super.onResume();
        askPermission();

        mTestedService = new ComponentName( this, LocatorJobService.class );
        mJobInfo = new JobInfo.Builder( JOB_ID, mTestedService )
                .setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY )
                .setRequiresDeviceIdle( false )
                .setRequiresCharging( false )
                /*
                 * Do not get tricked by this. Service will be started periodically only
                 * in wake mode or in maintenance window. When device enters the Doze mode
                 * the system will not allow JobScheduler to run any services.
                 * That's why if you need strict frequency called service use
                 * Alarms set with setAlarmClock()
                 */
                .setPeriodic( TimeUnit.SECONDS.toMillis( 15 ) )
                .build();
        mScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
    }

    @Override
    protected void onDestroy() {
        Log.d( LOG_TAG, "Service removed" );
        mScheduler.cancel( JOB_ID ); // remove this line, if you want service kept alive after activity destroy
        super.onDestroy();
    }

    public void onStartServiceButtonPressed (View view ) {
        int result_code = mScheduler.schedule(mJobInfo); //register our service
        if ( result_code == JobScheduler.RESULT_SUCCESS ) Log.d( LOG_TAG, "Scheduling successful!" );
        else if ( result_code == JobScheduler.RESULT_FAILURE ) Log.d( LOG_TAG, "Scheduling failed" );
    }

    public void onStopServiceButtonPressed ( View view ) {
        Log.d( LOG_TAG, "Service removed" );
        mScheduler.cancel( JOB_ID );
    }

    private void askPermission () {
        int fineLocationPermission = ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION );
        int coarseLocationPermission = ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION );
        ArrayList<String> permissionList = new ArrayList<>(2);
        if ( fineLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            permissionList.add( Manifest.permission.ACCESS_FINE_LOCATION );
        }
        if ( coarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            permissionList.add( Manifest.permission.ACCESS_COARSE_LOCATION );
        }
        if ( permissionList.size() > 0 ) {
            ActivityCompat.requestPermissions( this,
                    permissionList.toArray( new String[]{} ),
                    TEST_PERMISSION_REQUEST );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( requestCode != TEST_PERMISSION_REQUEST ) {
            Log.d( LOG_TAG, "How did you get here?" );
            return;
        }
        if ( grantResults.length > 0 ) {
            boolean permissionGranted = false;
            for ( int result : grantResults ) {
                if ( result == PackageManager.PERMISSION_GRANTED ) permissionGranted = true;
            }
            mStartButton.setClickable( permissionGranted );
        } else {
            Toast.makeText( this, "Permission denied", Toast.LENGTH_SHORT ).show();
        }
    }

    private static int getSomeRandomNumber () {
        return 4; // chosen by fair dice roll, guaranteed to be random
    }
}
