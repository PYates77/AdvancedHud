package hud.advancedhud_moverio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

/**
 * This class maintains a bluetooth connection with the google tango
 * it interprets bluetooth packets from the tango as wall coordinates
 *
 * To use: create a MoverioBluetooth object
 * call the connect() method to connect
 * call the getData() method for an array of wall objects
 * call the disconnect() method to disconnect
 *
 * to receive test data without requiring a bluetooth connection, call getTestData()
 *
 * the MoverioBluetooth thread should run as a background thread on the default processor
 *
 * Created by Paul on 3/8/2017.
 */

public class MoverioBluetooth extends AppCompatActivity {

    private final int BT_ENABLE_REQUEST_INIT = 0;
    private final int BT_ENABLE_REQUEST_CONNECT = 1;
    private final String DEBUG_TAG = "MoverioBluetooth";
    private final UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");

    private BluetoothAdapter btAdapter;
    private final BlockingQueue<Runnable> threadQueue;
    private final ThreadPoolExecutor threadPool;
    private Queue<Double> wallBuffer;

    private boolean connected = false;

    public MoverioBluetooth(){
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        wallBuffer = new LinkedList<Double>();
        if(btAdapter == null) {
            Log.e(DEBUG_TAG, "Unable to get BluetoothAdapter");
        }
        else if(!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_INIT);
        }
    }

    public void connect(){
        if(!btAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_CONNECT);
        }
        else{
            threadPool.execute(new ConnectThread());
        }

    }

    private class ConnectThread implements Runnable {
        private final String NAME = "MoverioServer";
        private final String DEBUG_TAG = "ConnectThread";
        private final BluetoothServerSocket btSocket;

        DataInputStream inStream;
        DataOutputStream outStream;

        public ConnectThread(){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            // initialise socket
            Log.d(DEBUG_TAG, "Server Thread Starting");

            BluetoothServerSocket tmp = null;
            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID);
            }
            catch (IOException e){
                Log.e(DEBUG_TAG, "Socket's listen() method failed", e);
            }
            btSocket = tmp;
            Log.d(DEBUG_TAG, "Created RfCommSocket with info: " + btSocket.toString());

        }

        public void run(){
            BluetoothSocket s = null;
            Log.d(DEBUG_TAG, "Waiting to accept socket");
            while (true) {
                try {
                    s = btSocket.accept();
                }
                catch (IOException e){
                    Log.e(DEBUG_TAG, "socket's accept() method failed", e);
                    break;
                }
                if (s != null){
                    Log.d(DEBUG_TAG, "A connection was accepted.");
                    IOSetup(s);
                    break;
                }
            }
        }

        private void IOSetup(BluetoothSocket s){
            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;
            Log.d(DEBUG_TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(s.getInputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating input stream",e);
            }
            try {
                tmpOut = new DataOutputStream(s.getOutputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating output stream",e);
            }
            inStream = tmpIn;
            outStream = tmpOut;
            communicate();
        }
        private void communicate(){
            Log.d(DEBUG_TAG, "Connection initiated. Waiting for data.");
            connected = true;

            while (true){
                try {
                    if(inStream.available() >= 32){
                        for(int i=0; i < 4; i++){

                            //TODO: add mutex
                            wallBuffer.add(inStream.readDouble());
                        }
                    }
                } catch (IOException e){
                    Log.e(DEBUG_TAG, "error while reading data",e );
                    connected = false;
                }
            }
        }
    }

    public boolean isConnected(){
        //TODO
        return connected;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_INIT){
            if(resultCode == RESULT_OK){
                Log.d(DEBUG_TAG,"Bluetooth Enabled by User");

            }
            else {
                Log.e(DEBUG_TAG,"Bluetooth is Disabled.");
            }
        }
        if(requestCode == BT_ENABLE_REQUEST_CONNECT){
            if(resultCode == RESULT_OK){
                connect(); //we call the connect thread again
            }
            else {
                Log.e(DEBUG_TAG, "Attempting to connect but bluetooth STILL not enabled");
            }
        }
    }

    public boolean dataReady(){
        //TODO
        return true;
    }
    public Wall[] getData(){
        //TODO: mutex lock
        Wall[] mWalls = new Wall[wallBuffer.size()/8];
        for( int i = 0; i< wallBuffer.size()/8; i++){
            Double a = wallBuffer.remove();
            Double b = wallBuffer.remove();
            Double c = wallBuffer.remove();
            Double d = wallBuffer.remove();
            Coordinate e = new Coordinate(a, b);
            Coordinate f = new Coordinate(c, d);
            mWalls[i] = new Wall(e,f);
        }
        return mWalls;
    }

    public Wall[] getTestData(){
        Wall[] mWalls = new Wall[8];
        Coordinate A = new Coordinate(90,400);
        Coordinate B = new Coordinate(175,400);
        Coordinate C = new Coordinate(90,140);
        Coordinate D = new Coordinate(175,190);
        Coordinate E = new Coordinate(-100,140);
        Coordinate F = new Coordinate(270,190);
        Coordinate G = new Coordinate(-100,90);
        Coordinate H = new Coordinate(90,90);
        Coordinate I = new Coordinate(90,40);
        Coordinate J = new Coordinate(270,40);
        mWalls[0] = new Wall(A,C);
        mWalls[1] = new Wall(C,E);
        mWalls[2] = new Wall(G,H);
        mWalls[3] = new Wall(H,I);
        mWalls[4] = new Wall(I,J);
        mWalls[5] = new Wall(J,F);
        mWalls[6] = new Wall(F,D);
        mWalls[7] = new Wall(D,B);
        return mWalls;
    }
}
