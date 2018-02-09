package com.fsprite.aarkh.locatorjobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.location.Location;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class LocatorJobService extends JobService {

    private static final String LOG_TAG = "LocatorJobServiceLogs";

    /*
     * if you have multiple threads, which could communicate with services vars it's
     * recommended to use volatile or synchronized key-word
     */
    private volatile Locator mLocator;

    public LocatorJobService() {
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        // The logic here run in the main thread, which means new threads should be used for any logic here
        Log.d( LOG_TAG, "onStartJob" );

        mLocator = new Locator( this );
        mLocator.requestLocationUpdates(); // already asynchronous
        mLocator.requestLastKnownLocation(); // already asynchronous
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Location location = mLocator.getLocation();
                //some calculations
                try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
                mLocator.removeLocationUpdates();
                jobFinished( jobParameters, false ); // call this in outer thread to report service has done it's work
            }
        });
        thread.start();
        return true; // letting the system know that service have a thread still running and it should hold on to wakelock for a while longer.
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d( LOG_TAG, "onStopJob" );
        return true;
    }

}
