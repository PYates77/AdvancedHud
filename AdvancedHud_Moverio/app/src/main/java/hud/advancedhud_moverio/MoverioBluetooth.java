package hud.advancedhud_moverio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Paul on 3/8/2017.
 */

public class MoverioBluetooth extends AppCompatActivity {

    private final int BT_ENABLE_REQUEST_RESULT_CODE = 0;
    private final String DEBUG_TAG = "MoverioBluetooth";

    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;

    public MoverioBluetooth(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Log.e(DEBUG_TAG, "Unable to get BluetoothAdapter");
        }
        if(!btAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, BT_ENABLE_REQUEST_RESULT_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_RESULT_CODE){
            if(resultCode == RESULT_OK){
                Log.d(DEBUG_TAG,"Bluetooth Enabled by User");

            }
            else {
                Log.e(DEBUG_TAG,"Bluetooth is Disabled.");
            }
        }
    }
    public double[] getData(){
        return new double[10];
    }

    /*DATA TO BE SENT
    //Demo Wall Map
        mWalls = new Wall[8];
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
     */



}
