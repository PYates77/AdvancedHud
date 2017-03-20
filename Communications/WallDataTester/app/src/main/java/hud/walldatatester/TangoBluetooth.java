package hud.walldatatester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Debug;
import android.os.Process;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class maintains a bluetooth connection with the moverio BT-200
 * it packages wall objects into bluetooth packets containing double arrays
 *
 * To use: create a TangoBluetooth object and pass it a BluetoothAdapter
 * call the connect() method to connect
 * call the send(double[]) method to send data to moverio
 * call the disconnect() method to disconnect
 *
 * Created by Paul on 3/20/2017.
 */

public class TangoBluetooth {
    private final String DEBUG_TAG = "TangoBluetooth";
    private final UUID MY_UUID = UUID.fromString("55ba6a24-f236-11e6-bc64-92361f002671");

    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private final BlockingQueue<Runnable> threadQueue;
    private final ThreadPoolExecutor threadPool;
    private Queue<Double> wallBuffer;
    private BluetoothSocket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    private boolean connected = false;

    public TangoBluetooth( BluetoothAdapter adapter){
        btAdapter = adapter;
        threadQueue = new LinkedBlockingQueue<Runnable>();
        int processor = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(processor, processor, 30, TimeUnit.SECONDS, threadQueue);
        wallBuffer = new LinkedList<Double>();
        connected = false;
    }

    public void connect() {
        if (!btAdapter.isEnabled()) {
            Log.e(DEBUG_TAG, "Bluetooth adapter not enabled. Cannot proceed");
        } else {
            String DEBUG_TAG = "ConnectThread";

            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice d : pairedDevices) {
                btDevice = d; //todo make this more dynamic
            }

            BluetoothSocket tmp = null;

            try {
                tmp = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Socket's create() method failed", e);
            }
            socket = tmp;
            Log.d(DEBUG_TAG, "Created RFCommSocket with info: " + socket.toString());

            btAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Unable to connect, closing socket", e);
                try {
                    socket.close();
                } catch (IOException c) {
                    Log.e(DEBUG_TAG, "Could not close the client socket", c);
                }

            }

            DataInputStream tmpIn = null;
            DataOutputStream tmpOut = null;

            Log.d(DEBUG_TAG, "Attempting to create input and output streams");
            try {
                tmpIn = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
            connected = true;
        }

    }

    //TODO make this run in background
    public void send(Double[] walls){ //array should be in format {x1, y1, x2, y2, ... }
        if(!connected){
            Log.e(DEBUG_TAG, "Bluetooth must be connected before sending.");
        }
        else{
            threadPool.execute(new Sender(walls));
        }
    }

    public class Sender implements Runnable {
        private Queue<Double> wallBuffer;

        public Sender(Double[] walls){
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            wallBuffer = new LinkedList<Double>();
            for(int i=0; i<walls.length; i++){
                wallBuffer.add(walls[i]);
            }
        }
        @Override
        public void run(){
            while(wallBuffer.size() > 0){
                try {
                    outStream.writeDouble(wallBuffer.remove());
                } catch (IOException e){
                    Log.e(DEBUG_TAG, "Unable to write Double to outStream");
                }
            }
        }
    }
}
