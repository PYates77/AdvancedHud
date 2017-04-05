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
        Point A = new Point(90,0,400);
        Point B = new Point(175,0,400);
        Point C = new Point(90,0,140);
        Point D = new Point(175,0,190);
        Point E = new Point(-100,0,140);
        Point F = new Point(270,0,190);
        Point G = new Point(-100,0,90);
        Point H = new Point(90,0,90);
        Point I = new Point(90,0,40);
        Point J = new Point(270,0,40);
        Wall2D w0 = new Wall2D(A);
        w0.addPoint(C);
        Wall2D w1 = new Wall2D(C);
        w1.addPoint(E);
        Wall2D w2 = new Wall2D(G);
        w2.addPoint(H);
        Wall2D w3 = new Wall2D(H);
        w3.addPoint(I);
        Wall2D w4 = new Wall2D(I);
        w4.addPoint(J);
        Wall2D w5 = new Wall2D(J);
        w5.addPoint(F);
        Wall2D w6 = new Wall2D(F);
        w6.addPoint(D);
        Wall2D w7 = new Wall2D(D);
        w7.addPoint(B);
        testList.add(w0);
        testList.add(w1);
        testList.add(w2);
        testList.add(w3);
        testList.add(w4);
        testList.add(w5);
        testList.add(w6);
        testList.add(w7);
//        mWalls[0] = new Wall(A,C);
//        mWalls[1] = new Wall(C,E);
//        mWalls[2] = new Wall(G,H);
//        mWalls[3] = new Wall(H,I);
//        mWalls[4] = new Wall(I,J);
//        mWalls[5] = new Wall(J,F);
//        mWalls[6] = new Wall(F,D);
//        mWalls[7] = new Wall(D,B);
//        for (int i=0; i <= 10; i++){
//            Wall2D w = new Wall2D(new Point(i+0.1,i+0.2,i+0.3));
//            w.addPoint(new Point(i+0.4,i+0.5,i+0.6));
//            testList.add(w);
//            Log.d("Wall Init","Created wall " + w.to_string());
//        }
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
