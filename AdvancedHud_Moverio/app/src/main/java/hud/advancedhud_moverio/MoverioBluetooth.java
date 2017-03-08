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
        
    }


}
