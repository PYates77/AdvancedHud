/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.examples.java.AdvancedHUD;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import org.rajawali3d.surface.RajawaliSurfaceView;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main Activity class for the Motion Tracking API Sample. Handles the connection to the Tango
 * service and propagation of Tango pose data to OpenGL and Layout views. OpenGL rendering logic is
 * delegated to the {@link MotionTrackingRajawaliRenderer} class.
 */
public class MotionTrackingActivity extends Activity {

    private static final String TAG = MotionTrackingActivity.class.getSimpleName();
    private static final int NUM_CLUSTERS = 100;

    private RajawaliSurfaceView mSurfaceView;
    private MotionTrackingRajawaliRenderer mRenderer;
    private Tango mTango;
    private TangoConfig mConfig;

    private AtomicBoolean mIsTangoPoseReady = new AtomicBoolean(false);

    private int mCurrentDisplayOrientation = 0;
    private KMeans kmeans;
    private HUD_User hud_user;
    private HashMap<Integer, Plane> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_tracking);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();

        // Initialize Tango Service as a normal Android Service, since we call mTango.disconnect()
        // in onPause, this will unbind Tango Service, so every time when onResume gets called, we
        // should create a new Tango object.
        mTango = new Tango(MotionTrackingActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there is no UI
            // thread changes involved.
            @Override
            public void run() {
                // Synchronize against disconnecting while the service is being used in the OpenGL
                // thread or in the UI thread.
                synchronized (MotionTrackingActivity.this) {
                    try {
                        mConfig = setupTangoConfig(mTango);
                        mTango.connect(mConfig);
                        startupTango();
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.exception_out_of_date), e);
                        showsToastAndFinishOnUiThread(R.string.exception_out_of_date);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.exception_tango_error), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_error);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.exception_tango_invalid), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_invalid);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (this) {
            try {
                mTango.disconnect();
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.exception_tango_error), e);
            }
        }
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Create a new Tango Configuration and enable the HelloMotionTrackingActivity API.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

        // Tango service should automatically attempt to recover when it enters an invalid state.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        return config;
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to pose data.
     */
    private void startupTango() {
        // Lock configuration and connect to Tango
        // Select coordinate frame pair
        final ArrayList<TangoCoordinateFramePair> framePairs =
                new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Listen for new Tango data
        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
                logPose(pose);
                hud_user.update_pose(pose);
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            ArrayList<Point> to_point_list(float[] arr) {
                ArrayList<Point> out = new ArrayList<Point>(arr.length/4);

                for (int i = 0; i < arr.length; i += 4)
                    out.add(new Point(arr[i], arr[i+1], arr[i+2]));

                return out;
            }

            @Override
            public void onPointCloudAvailable(final TangoPointCloudData pointCloudData) {
                // We are not using onPointCloudAvailable for this app.
                float[] arr = pointCloudData.points.array();
                ArrayList<Point> points = to_point_list(arr);

                kmeans = new KMeans(points, NUM_CLUSTERS); // generating planes for all clusters

                List<Cluster> planes = kmeans.getPointsClusters();
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
                // Ignoring TangoEvents.
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    private void logPose(TangoPoseData pose) {
        StringBuilder stringBuilder = new StringBuilder();

        float translation[] = pose.getTranslationAsFloats();
        stringBuilder.append("Position: " +
                translation[0] + ", " + translation[1] + ", " + translation[2]);

        float orientation[] = pose.getRotationAsFloats();
        stringBuilder.append(". Orientation: " +
                orientation[0] + ", " + orientation[1] + ", " +
                orientation[2] + ", " + orientation[3]);

        Log.i(TAG, stringBuilder.toString());
    }


    /**
     * Connects the Rajawali surface view and its renderer. This is ideally called only once in
     * onCreate.
     */
    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MotionTrackingActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
