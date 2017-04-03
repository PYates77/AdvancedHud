package hud.walldatatester;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random r = new Random();

    private final int BT_ENABLE_REQUEST_INIT = 0;
    private final int BT_ENABLE_REQUEST_CONNECT = 1;

    TangoBluetooth btManager;
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Generated Canned Data for transmission test
        ArrayList<Wall2D> testList = new ArrayList<Wall2D>();
        for (int i=0; i <= 5; i++){
            Wall2D w = new Wall2D(new Point(i+0.1,i+0.2,i+0.3));
            w.addPoint(new Point(i+0.4,i+0.5,i+0.6));
            testList.add(w);
            Log.d("Wall Init","Created wall " + w.to_string());
        }
        //this testlist is what needs to be sent over

        startBluetoothAdapter();
        btManager = new TangoBluetooth(btAdapter);
        do {
            btManager.connect();
        } while (!btManager.isConnected());

        for(Wall2D w : testList){
            btManager.send(w.sendData());
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
                btManager.connect(); //attempt to connect again
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
