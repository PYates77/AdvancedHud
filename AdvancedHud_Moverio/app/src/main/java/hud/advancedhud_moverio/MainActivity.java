package hud.advancedhud_moverio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
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
    public Wall[] wallList;
    public boolean readyFlag = false;

    Thread updateTextViewThread = new Thread(){
        public void run(){
            while(true){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(readyFlag){
                            mapView.invalidate();
                            mapDrawable.setWallArray(wallList);
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


    public MainActivity(){
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
        wallList = new Wall[8];

        updateTextViewThread.start();

        startBluetoothAdapter();
        btManager = new MoverioBluetooth(btAdapter);

        btManager.connect();
        if(btAdapter.isEnabled()) {
            threadPool.execute(new DataFetcher());
        }


        
    }

    public class DataFetcher implements Runnable{
        public void run(){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            //todo actually render the walls using akshay's code
            while(btAdapter.isEnabled()){
                if(btManager.isConnected() && !btManager.isConnecting()) {
                    wallList = btManager.getData();
                    if(wallList != null) {
                        Log.d("DataFetcher", "Received wallList: Length: " + wallList.length);
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
                else if(!btManager.isConnected() && !btManager.isConnecting()){
                    btManager.connect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_INIT){
            if(resultCode == RESULT_OK){
                Log.d("Activity Result","Bluetooth Enabled by User");

            }
            else {
                Log.e("ActivityResult","Bluetooth is Disabled.");
            }
        }
        if(requestCode == BT_ENABLE_REQUEST_CONNECT){
            if(resultCode == RESULT_OK){
                btManager.connect(); //now that bt is enabled we attempt to connect again
                threadPool.execute(new DataFetcher());
            }
            else {
                Log.e("ActivityResult", "Attempting to connect but bluetooth STILL not enabled");
            }
        }
    }
    protected void startBluetoothAdapter(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Log.e("StartBluetoothAdapter", "Unable to get BluetoothAdapter");
        }
        else if(!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_INIT);
        }
    }

}
