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

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Main Activity class for the Depth Perception Sample. Handles the connection to the {@link Tango}
 * service and propagation of Tango PointCloud data to Layout view.
 */
public class HelloDepthPerceptionActivity extends Activity {

    private static final String TAG = HelloDepthPerceptionActivity.class.getSimpleName();
    private static final int SAMPLE_FACTOR = 10;
    private static final int NUM_CLUSTERS = 10;
    private static final double angleMargin = Math.PI / 6.0;
    private static final double distanceMargin = 0.5; // needs to be determined

    // 2-D attempt
    private static final int numGroups = 160;
    private static final int minPointCount = 10;
    
    private ArrayList<Point> global_points;

    private Tango mTango;
    private TangoConfig mConfig;

    // Yotam's Classes
    private HUD_User hud_user = new HUD_User();
    private KMeans kmeans;
    private ArrayList<Wall> wallList = new ArrayList<Wall>();
    private ArrayList<Wall2D> wall2DList = new ArrayList<Wall2D>();
    private ArrayList<Wall2D> wallOutList = new ArrayList<Wall2D>();
    private Matrix gMatrix;

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
                        mapDrawable.setPointArray(global_points);
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
                //hud_user.update_pose(pose);
                gMatrix = calcGMatrix(pose);
//                logPose(hud_user.getPose());
//                logPose(pose);
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            //WHY ARE THERE RETURN STATEMENTS IN THE FOLLOWING CODE? WHAT ARE THESE METHODS?
            ArrayList<Point> to_point_list(FloatBuffer arr) {
                ArrayList<Point> out = new ArrayList<Point>();

                for (int i = 0; i < arr.limit(); i += 4) {
                    double[] currPoint = {arr.get(i), arr.get(i+1), arr.get(i+2), arr.get(i+3)};
                    Matrix pointMat = new Matrix(4, 1, currPoint);
                    try {
                        pointMat = gMatrix.multiply(pointMat);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    out.add(new Point(pointMat.getElement(0), pointMat.getElement(1), pointMat.getElement(2)));
                }

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
                    Plane xyPlane = new Plane(p1, p2, p3);

                    a.get(i).calcPlane();
                    if (a.get(i).getPlane() != null) {
                        if (a.get(i).getPlane().calcInterPlaneAngle(xyPlane) > angleMargin) {
                            boolean condition = false;
                            for (int j = 0; j < wallList.size() && !condition; j++) {
                                if (wallList.get(j).getPlane() != null) {
                                    double angle = wallList.get(j).getPlane().calcInterPlaneAngle(a.get(i).getPlane());

                                    if (angle < angleMargin && a.get(i).getPlane().getShift() < distanceMargin) {
                                        wallList.get(j).update(a.get(i));
                                        condition = true; // wall found
                                    }

                                    if (angle < Math.PI/2.0 - angleMargin && angle > angleMargin) {
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

            private void modifyOutList() {
                for (int i = 0; i < wall2DList.size(); i++) {
                    Wall2D curWall = wall2DList.get(i);

                    if (curWall.getPointCount() > minPointCount) {
                        boolean found = false;

                        int j = 0;
                        while (!false && j < wallOutList.size()) {
                            if (curWall.getAngle(wallOutList.get(j)) < angleMargin) {
                                found = true;
                            }

                            j++;
                        }

                        if (!found)
                            wallOutList.add(curWall);
                    } else
                        Log.i(TAG, String.valueOf(curWall.getPointCount()));
                }
            }

            private void modify2DWallList(ArrayList<Point> points) {
                if (points == null)
                    return;

                boolean wallFound = false;

                for (int i = 0; i < points.size(); i++) {

                    Point curPoint = points.get(i);
                    for (int j = 0; j < wall2DList.size(); j++) {
                        if (wall2DList.get(j).getDistance(curPoint) < distanceMargin) {
                            wall2DList.get(j).addPoint(curPoint);
                            wallFound = true;
                        }
                    }

                    if (!wallFound) {
                        wall2DList.add(new Wall2D(curPoint));
                    }
                }
            }

            // attempt to calc point cloud trend line
            private Line linearRegression(ArrayList<Point> points) {
                double sumx = 0.0, sumz = 0.0, sumx2 = 0.0;

                for (int i = 0; i < points.size(); i++) {
                    Point curPoint = points.get(i);
                    sumx += curPoint.x;
                    sumz += curPoint.z;
                    sumx2 += curPoint.x*curPoint.x;
                }

                double xbar = sumx/((double) points.size());
                double zbar = sumz/((double) points.size());

                double xxbar = 0.0, xzbar = 0.0;

                for (int i = 0; i < points.size(); i++) {
                    Point curPoint = points.get(i);
                    xxbar += (curPoint.x - xbar) * (curPoint.x - xbar);
                    xzbar += (curPoint.x - xbar) * (curPoint.z - zbar);
                }

                double slope = xzbar / xxbar;
                double intercept = zbar - slope * xbar;

                return new Line(slope, intercept);
            }

            private ArrayList<Point> generateAverages(ArrayList<Point> allPoints) {
                ArrayList<Point> averagedPoints = new ArrayList<Point>();
                
                if (allPoints.size() > 0) {
                
                    for (int i = 0; i < numGroups; i++) {
                        int groupSize = allPoints.size()/numGroups;
                        int start = i*groupSize;
                        Point avg = allPoints.get(start);
                      
                        for (int j = start + 1; j < start + groupSize; j++)
                            avg.add(allPoints.get(j));
                      
                        avg.x /= groupSize;
                        avg.y /= groupSize;
                        avg.z /= groupSize;
                        
                        averagedPoints.add(avg);
                    }
                
                }
                
                return averagedPoints;
            }

            private ArrayList<Point> getDisplayPoints() {
                double numPointPerLine = 100.0;
                ArrayList<Point> outPoints = new ArrayList<>();

                for (int i = 0; i < wallOutList.size(); i++) {

                    Point start = wallOutList.get(i).getEdge1();
                    Point direction = wallOutList.get(i).getEdge2();
                    direction.subtract(start);

                    for (int j = 1; j <= numPointPerLine; j++) {
                        outPoints.add(addPoints(start, new Point(direction.x*0.01*j, direction.y*0.01*j, direction.z*0.01*j)));
                    }
                }

                return outPoints;
            }

            public Point addPoints(Point p1, Point p2) {
                double rx = p1.x + p2.x;
                double ry  = p1.y + p2.y;
                double rz = p1.z + p2.z;

                return new Point(rx, ry, rz);
            }

            @Override
            public void onPointCloudAvailable(final TangoPointCloudData pointCloudData) {

                if (pointCloudData.points == null) {
                    Log.i(TAG, "pointCloudData.points is NULL");
                } else {
                    FloatBuffer arr  = pointCloudData.points;
                    ArrayList<Point> points = to_point_list(arr);

                    points = generateAverages(points);
                    //Log.i(TAG, String.valueOf(global_points.get(0)));
                    modify2DWallList(points);

                    modifyOutList();

                    Log.i(TAG, String.valueOf(wallOutList.get(0).getLine().getSlope()));
                    Log.i(TAG, String.valueOf(wallOutList.get(0).getLine().getIntercept()));

                    // global_points = getDisplayPoints();
                    
                    global_points = points;
                    
                    /*global_points = points;
                    points = sample_array(points);

                    kmeans = new KMeans(points, NUM_CLUSTERS); // generate clusters from point cloud
                    if (kmeans.allPoints == null) {
                        Log.i(TAG, "kmeans.allPoints is NULL");
                    } else {
                        ArrayList<Cluster> clusters = (ArrayList<Cluster>) kmeans.getPointsClusters();

                        modifyWallList(clusters);
                    }*/

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

    private Matrix calcGMatrix(TangoPoseData pose) {
        float translation[] = new float[3];
        float orientation[] = new float[4];
        float currMatrix[] = new float[16];
        translation = pose.getTranslationAsFloats();
        orientation = pose.getRotationAsFloats();
        float qw = orientation[0];
        float qx = orientation[1];
        float qy = orientation[2];
        float qz = orientation[3];
        //Extracting Rotation Matrix from orientation quaternion
        double[] arr = new double[16];
        Matrix result = new Matrix(4, 4, arr);

        result.setElement(1-2*(qy*qy)-2*(qz*qz), 0);
        result.setElement((2*qx*qy)+(2*qz*qw), 1);
        result.setElement((2*qx*qz)-(2*qy*qw), 2);
        result.setElement((2*qx*qy)-(2*qz*qw), 4);
        result.setElement(1-(2*qx*qx)-(2*qz*qz), 5);
        result.setElement((2*qy*qz)+(2*qx*qw), 6);
        result.setElement((2*qx*qz)+(2*qy*qw), 8);
        result.setElement((2*qy*qz)-(2*qx*qw), 9);
        result.setElement(1-(2*qx*qx)-(2*qy*qy), 10);
        //Extracting translation matrix
        result.setElement(translation[0], 3);
        result.setElement(translation[1], 7);
        result.setElement(translation[2], 11);
        //Populating final aspect of 4X4 matrix
        result.setElement(0, 12);
        result.setElement(0, 13);
        result.setElement(0, 14);
        result.setElement(1, 15);
        return result;
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
