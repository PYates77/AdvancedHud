package hud.walldatatester;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random r = new Random();

    private final int BT_ENABLE_REQUEST_INIT = 0;
    private final int BT_ENABLE_REQUEST_CONNECT = 1;
    private volatile boolean btAdapterResponse = false;
    private final Double FRAME_START = Double.NEGATIVE_INFINITY;
    private final Double FRAME_DELIMITER = Double.MAX_VALUE;
    private final Double FRAME_END = Double.POSITIVE_INFINITY;

    TangoBluetooth btManager;
    BluetoothAdapter btAdapter;

    Button testListButton1;
    Button testListButton2;
    Button testListButton3;
    Button testListButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btManager = null;


        testListButton1 = (Button) findViewById(R.id.sendWalls1);
        testListButton2 = (Button) findViewById(R.id.sendWalls2);
        testListButton3 = (Button) findViewById(R.id.sendWalls3);
        testListButton4 = (Button) findViewById(R.id.sendWalls4);

        testListButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList1();
                        ArrayList<Double> oriList = new ArrayList<>();

                        Double posX = 0.0;
                        Double posY = 0.0;
                        Double heading = 0.0;
                        oriList.add(posX);
                        oriList.add(posY);
                        oriList.add(heading);
                        btManager.send(makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList2();
                        ArrayList<Double> oriList = new ArrayList<>();

                        Double posX = 0.0;
                        Double posY = 0.0;
                        Double heading = 0.0;
                        oriList.add(posX);
                        oriList.add(posY);
                        oriList.add(heading);
                        btManager.send(makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList3();
                        ArrayList<Double> oriList = new ArrayList<>();

                        Double posX = 0.0;
                        Double posY = 0.0;
                        Double heading = 0.0;
                        oriList.add(posX);
                        oriList.add(posY);
                        oriList.add(heading);
                        btManager.send(makeFrame(oriList,wallList));
                    }
                }
            }
        });
        testListButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btManager != null){
                    if(btManager.isConnected()){
                        ArrayList<Wall2D> wallList = testList4();
                        ArrayList<Double> oriList = new ArrayList<>();

                        Double posX = 0.0;
                        Double posY = 0.0;
                        Double heading = 0.0;
                        oriList.add(posX);
                        oriList.add(posY);
                        oriList.add(heading);
                        btManager.send(makeFrame(oriList,wallList));
                    }
                }
            }
        });

        //this testlist is what needs to be sent over

        startBluetoothAdapter();
        if(btAdapter.isEnabled()) {
            btManager = new TangoBluetooth(btAdapter);
            do {
                btManager.connect();
            } while (!btManager.isConnected());

            //Generated Canned Data for transmission test
            ArrayList<Wall2D> wallList = testList1();
            ArrayList<Double> oriList = new ArrayList<>();

            Double posX = 0.0;
            Double posY = 0.0;
            Double heading = 0.0;
            oriList.add(posX);
            oriList.add(posY);
            oriList.add(heading);
            btManager.send(makeFrame(oriList,wallList));
        }
        else {
            Log.e("MainActivity","FATAL ERROR: Bluetooth adapter must be enabled to communicate.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BT_ENABLE_REQUEST_INIT){
            btAdapterResponse = true;
            if(resultCode == RESULT_OK){
                Log.d("Activity Result","Bluetooth Enabled by User");
            }
            else {
                Log.e("ActivityResult","Bluetooth is Disabled.");
            }
        }
        if(requestCode == BT_ENABLE_REQUEST_CONNECT){
            if(resultCode == RESULT_OK){
                //btManager.connect(); //attempt to connect again
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
            while(!btAdapterResponse){} //block until an answer is received
        }
    }
    protected ArrayList<Wall2D> testList1(){
        ArrayList<Wall2D> testList = new ArrayList<>();
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
        return testList;
    }
    protected ArrayList<Wall2D> testList2(){
        ArrayList<Wall2D> testList = new ArrayList<>();
        Point A = new Point(50,0,500);
        Point B = new Point(100,0,300);
        Point C = new Point(80,0,160);
        Point D = new Point(125,0,200);
        Point E = new Point(-60,0,120);
        Point F = new Point(280,0,200);
        Point G = new Point(-120,0,60);
        Point H = new Point(100,0,100);
        Point I = new Point(80,0,50);
        Point J = new Point(290,0,30);
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
        return testList;
    }
    protected ArrayList<Wall2D> testList3(){
        ArrayList<Wall2D> testList = new ArrayList<>();
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
        return testList;
    }
    protected ArrayList<Wall2D> testList4(){
        ArrayList<Wall2D> testList = new ArrayList<>();
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
        return testList;
    }
    protected Double[] makeFrame(ArrayList<Double> orientation, ArrayList<Wall2D> walls){
        ArrayList<Double> dataFrame = new ArrayList<>();
        dataFrame.add(FRAME_START);
        for (Double d : orientation){
            dataFrame.add(d);
        }
        dataFrame.add(FRAME_DELIMITER);
        Double[] tmp;
        for (Wall2D w : walls) {
            tmp = w.sendData();
            dataFrame.add(tmp[0]);
            dataFrame.add(tmp[1]);
            dataFrame.add(tmp[2]);
            dataFrame.add(tmp[3]);
        }
        dataFrame.add(FRAME_END);

        Double[] doubles = new Double[dataFrame.size()];
        for (int i = 0; i < dataFrame.size(); i++) {
            doubles[i] = dataFrame.get(i);
        }
        return doubles;
    }
}
