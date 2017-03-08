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

package pt.advHUD;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import pt.advHUD.Plane;
import pt.advHUD.Point;
import pt.advHUD.R;
import pt.advHUD.Wall;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Main Activity class for the Depth Perception Sample. Handles the connection to the {@link Tango}
 * service and propagation of Tango PointCloud data to Layout view.
 */
public class HelloDepthPerceptionActivity extends Activity {

    private static final String TAG = HelloDepthPerceptionActivity.class.getSimpleName();
    private static final int SAMPLE_FACTOR = 10;
    private static final int NUM_CLUSTERS = 10;
    private static final double angleMargin = Math.PI / 12;

    private Tango mTango;
    private TangoConfig mConfig;

    // Yotam's Classes
    private HUD_User hud_user = new HUD_User();
    private KMeans kmeans;
    private ArrayList<Wall> wallList = new ArrayList<Wall>();

    private ImageView mapView;
    private MapDrawable mapDrawable;

    //Setup new thread to control UI view updates --> THIS IS A BIT SLOW WARNING!
    Thread updateTextViewThread = new Thread(){
        public void run(){
            while(true){
                HelloDepthPerceptionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapView.invalidate();
                        mapDrawable.setWallArray(wallList);
                    }
                });
                try {
                    Thread.sleep(500); //2Hz refresh rate
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depth_perception);

        mapView = (ImageView)findViewById(R.id.mapView);
        mapDrawable = new MapDrawable();
        mapView.setImageDrawable(mapDrawable);
        updateTextViewThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize Tango Service as a normal Android Service, since we call mTango.disconnect()
        // in onPause, this will unbind Tango Service, so every time when onResume gets called, we
        // should create a new Tango object.
        mTango = new Tango(HelloDepthPerceptionActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there is no UI
            // thread changes involved.
            @Override
            public void run() {
                synchronized (HelloDepthPerceptionActivity.this) {
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
        // Create a new Tango Configuration and enable the Depth Sensing API.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
        return config;
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to new Point Cloud data.
     */
    private void startupTango() {
        // Lock configuration and connect to Tango.
        // Select coordinate frame pair.
        final ArrayList<TangoCoordinateFramePair> framePairs =
                new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Listen for new Tango data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
                hud_user.update_pose(pose);
//                logPose(hud_user.getPose());
//                logPose(pose);
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            ArrayList<Point> to_point_list(FloatBuffer arr) {
                ArrayList<Point> out = new ArrayList<Point>();

                for (int i = 0; i < arr.limit(); i += 4)
                    out.add(new Point(arr.get(i), arr.get(i+1), arr.get(i+2)));

                return out;
            }

            private ArrayList<Point> sample_array(ArrayList<Point> a) {
                int size = a.size()/SAMPLE_FACTOR;
                ArrayList<Point> out = new ArrayList<Point>(size);

                for (int i = 0; i < size; i++)
                    out.add(a.get(i*SAMPLE_FACTOR));

                return out;
            }

            private void modifyWallList(ArrayList<Cluster> a) {
                if (a == null)
                    return;

                for (int i = 0; i < a.size(); i++) {
                    Point p1 = new Point(0, 0, 0);
                    Point p2 = new Point(1, 0, 0);
                    Point p3 = new Point(0, 1, 0);
                    Plane xyPlane(p1, p2, p3);
                    
                    a.get(i).calcPlane(xyPlane);
                    if (a.get(i).getPlane() != null) {
                        if (a.get(i).gePlane().calcInterPlaneAngle(xyPlane) < angleMargin) {
                            boolean condition = false;
                            for (int j = 0; j < wallList.size() && !condition; j++) {
                                if (wallList.get(j).getPlane() != null) {
                                    double angle = wallList.get(j).getPlane().calcInterPlaneAngle(a.get(i).getPlane());

                                    if (angle < angleMargin) {
                                        wallList.get(j).update(a.get(i));
                                        condition = true; // wall found
                                    } else if (angle < Math.PI/2.0 - angleMargin) {
                                        condition = true; // cluster plane in dead zone
                                    }
                                }
                            }

                            if (!condition) {
                                wallList.add(new Wall(a.get(i)));
                            }
                        }
                    }
                }
            }

            @Override
            public void onPointCloudAvailable(final TangoPointCloudData pointCloudData) {

                if (pointCloudData.points == null) {
                    Log.i(TAG, "pointCloudData.points is NULL");
                } else {
                    FloatBuffer arr  = pointCloudData.points;
                    ArrayList<Point> points = to_point_list(arr);
                    points = sample_array(points);

                    // GENERATE TEST POINT CLOUD ---------------------------------
//                    ArrayList<Point> points = new ArrayList<Point>();
//
//                    for (int i = 0; i < 1000; i++) {
//                        points.add(new Point(Math.random(), Math.random(), 0));
//                    }
                    // -----------------------------------------------------------

                    kmeans = new KMeans(points, NUM_CLUSTERS); // generate clusters from point cloud
                    if (kmeans.allPoints == null) {
                        Log.i(TAG, "kmeans.allPoints is NULL");
                    } else {
                        ArrayList<Cluster> clusters = (ArrayList<Cluster>) kmeans.getPointsClusters();

//                        // PRINT ANGLE BETWEEN FIRST CLUSTER AND ALL OTHERS -------------------------------------------------------
//                        clusters.get(0).calcPlane();
//                        Cluster testCluster = clusters.get(0);
//                        for (int i = 1; i < clusters.size(); i++) {
//                            clusters.get(i).calcPlane();
////                            Log.i(TAG, new Double(clusters.get(i).getPlane().calcInterPlaneAngle(testCluster.getPlane())*(180.0 / Math.PI)).toString());
////                            Log.i(TAG, "Cluster[" + String.valueOf(i) + "] = ");
////                            Log.i(TAG, "x = " + String.valueOf(clusters.get(i).getPlane().getDirection().x) + " y = " + String.valueOf(clusters.get(i).getPlane().getDirection().y) + " z = " + String.valueOf(clusters.get(i).getPlane().getDirection().z));
//
//                        }
//                        Log.i(TAG, "----boop----");
                        // --------------------------------------------------------------------------------------------------------

                        modifyWallList(clusters);
//
//                        //Log.i(TAG, new Boolean(clusters == null).toString());
//
//                         PRINT LENGTH OF WALLS
                        Plane plane = new Plane(new Point(0, 0, 0), new Point(0, 1, 0), new Point(1, 0, 0));
                        for (int i = 0; i < wallList.size(); i++) {
                            if (wallList.get(i).isValid()) {
                                Log.i(TAG, "wall " + i + " angle = " + String.valueOf(wallList.get(i).getPlane().calcInterPlaneAngle(plane)));
                                Log.i(TAG,"\nWall " +i+" : "+ wallList.get(i).getEdge1().x +" "+wallList.get(i).getEdge1().y+" "+wallList.get(i).getEdge2().x+" "+wallList.get(i).getEdge2().y+"\n"); //debug wall values AKSHAY
                            }
                        }
                        Log.i(TAG, new Integer(wallList.size()).toString());
                    }

                }
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

    /**
     * Log the Position and Orientation of the given pose in the Logcat as information.
     *
     * @param pose the pose to log.
     */
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
     * Log the point count and the average depth of the given PointCloud data
     * in the Logcat as information.
     */
    private void logPointCloud(TangoPointCloudData pointCloudData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Point count: " + pointCloudData.numPoints);
        stringBuilder.append(". Average depth (m): " +
                calculateAveragedDepth(pointCloudData.points, pointCloudData.numPoints));
        Log.i(TAG, stringBuilder.toString());
    }

    /**
     * Calculates the average depth from a point cloud buffer.
     */
    private float calculateAveragedDepth(FloatBuffer pointCloudBuffer, int numPoints) {
        float totalZ = 0;
        float averageZ = 0;
        if (numPoints != 0) {
            int numFloats = 4 * numPoints;
            for (int i = 2; i < numFloats; i = i + 4) {
                totalZ = totalZ + pointCloudBuffer.get(i);
            }
            averageZ = totalZ / numPoints;
        }
        return averageZ;
    }

    /**
     * Display toast on UI thread.
     *
     * @param resId The resource id of the string resource to use. Can be formatted text.
     */
    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HelloDepthPerceptionActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
