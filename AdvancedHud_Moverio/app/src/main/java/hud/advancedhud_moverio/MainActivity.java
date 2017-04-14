package hud.advancedhud_moverio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    MoverioBluetooth btManager;
    BluetoothAdapter btAdapter;

    private final BlockingQueue<Runnable> threadQueue;
    private final ThreadPoolExecutor threadPool;

    private final int BT_ENABLE_REQUEST_INIT = 0;
    private final int BT_ENABLE_REQUEST_CONNECT = 1;

    private MapDrawable mapDrawable = new MapDrawable();
    private ImageView mapView;
    public ArrayList<Wall> mWallList = new ArrayList<Wall>();
    public boolean readyFlag = false;
    public float[] translation = new float[3];
    public float[] orientation = new float[4];
    private float rollr = 0;

    //for testing
    private TextView transView;
    private TextView orientView;
    private String displayTrans;
    private String displayOrient;

    Thread updateTextViewThread = new Thread() {
        public void run() {
            while (true) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (readyFlag) {
                            mapView.invalidate();
                            mapDrawable.setDynamicWallArray(mWallList);
                            //for testing
                            transView.setText(displayTrans);
                            orientView.setText(displayOrient);
                        }
                    }
                });
                try {
                    Thread.sleep(500); //2Hz Refresh Rate
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public MainActivity() {
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getActionBar().hide();

        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        //winParams.flags |= WindowManager.LayoutParams.FLAG_SMARTFULLSCREEN;
        winParams.flags |= 0x80000000;
        win.setAttributes(winParams);

        mapView = (ImageView) findViewById(R.id.mapView);
        mapView.setImageDrawable(mapDrawable);

        //for debug
        transView = (TextView)findViewById(R.id.translationView);
        orientView = (TextView)findViewById(R.id.orientView);

        //follow for loop and assigment statement is to initialize the orientation and translation array to zeroes
        for (int i = 0; i < 3; i++) {
            translation[i] = 0;
            orientation[i] = 0;
        }
        orientation[3] = 0;

        //start the mapview updating thread
        updateTextViewThread.start();

        startBluetoothAdapter();
        btManager = new MoverioBluetooth(btAdapter);

        btManager.connect();
        if (btAdapter.isEnabled()) {
            threadPool.execute(new DataFetcher());
        }


    }

    public class DataFetcher implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            while (btAdapter.isEnabled()) {
                if (btManager.isConnected() && !btManager.isConnecting()) {
                    Wall[] wallList = btManager.getData();
                    Double[] oL = btManager.getOrientation();
                    if (wallList != null && oL != null) {
                        mWallList.clear();
                        Log.d("DataFetcher", "Received wallList: Length: " + wallList.length);
                        for (int i = 0; i < wallList.length; i++) {
                            mWallList.add(wallList[i]);
                        }
                        translation[0] = oL[0].floatValue();
                        translation[1] = oL[1].floatValue();
                        translation[1] = oL[2].floatValue();
                        orientation[0] = oL[3].floatValue();
                        orientation[1] = oL[4].floatValue();
                        orientation[2] = oL[5].floatValue();
                        orientation[3] = oL[6].floatValue();
                        updateLocation();
                        readyFlag = true;
                    }
//                    if (walls != null) {
//                        for (int i = 0; i < walls.length; i++) {
//                            //Log.d("Receiving", "Got wall data: " + walls[i]);
//                            Double[] w = walls[i].getDoubleArray();
//                            Log.d("Receiving", "Wall " + i + " has Start = (" + w[0] + "," + w[1] + ")  End = (" + w[2] + "," + w[3] + ")");
//                        }
//                    }
                }
                //attempt to reconnect if the manager is neither connected nor currently attempting to connect
                else if (!btManager.isConnected() && !btManager.isConnecting()) {
                    btManager.connect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_ENABLE_REQUEST_INIT) {
            if (resultCode == RESULT_OK) {
                Log.d("Activity Result", "Bluetooth Enabled by User");
                btManager.connect();
                threadPool.execute(new DataFetcher());
            } else {
                Log.e("ActivityResult", "Bluetooth is Disabled.");
            }
        }
        if (requestCode == BT_ENABLE_REQUEST_CONNECT) {
            if (resultCode == RESULT_OK) {
                btManager.connect(); //now that bt is enabled we attempt to connect again
                threadPool.execute(new DataFetcher());
            } else {
                Log.e("ActivityResult", "Attempting to connect but bluetooth STILL not enabled");
            }
        }
    }

    protected void startBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Log.e("StartBluetoothAdapter", "Unable to get BluetoothAdapter");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_INIT);
        }
    }

    private void updateLocation() {
        //for testing
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb1.append("Position: "+translation[0]+", "+translation[1]+", "+translation[2]);
        sb2.append("Orientation: "+orientation[0]+", "+orientation[1]+", "+orientation[2]+", "+orientation[3]);
        displayTrans = sb1.toString();
        displayOrient = sb2.toString();
        //obtaining rotation matrix using quaternion notation
        /*
        float qw = orientation[0];
        float qx = orientation[1];
        float qy = orientation[2];
        float qz = orientation[3];
        //Extract Rotation Matrix
        float[] rotMatrix = new float[9];
        rotMatrix[0] = 1 - 2 * (qy * qy) - 2 * (qz * qz);
        rotMatrix[1] = (2 * qx * qy) + (2 * qz * qw);
        rotMatrix[2] = (2 * qx * qz) - (2 * qy * qw);
        rotMatrix[3] = (2 * qx * qy) - (2 * qz * qw);
        rotMatrix[4] = 1 - (2 * qx * qx) - (2 * qz * qz);
        rotMatrix[5] = (2 * qy * qz) + (2 * qx * qw);
        rotMatrix[6] = (2 * qx * qz) + (2 * qy * qw);
        rotMatrix[7] = (2 * qy * qz) - (2 * qx * qw);
        rotMatrix[8] = 1 - (2 * qx * qx) - (2 * qy * qy);
        //Get orientation information
        float euOrient[] = new float[3];
        SensorManager.getOrientation(rotMatrix, euOrient);
        rollr = (float) Math.toDegrees(euOrient[2]);
        */

    }
}
