package hud.walldatatester;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Random r = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Generated Canned Data for transmission test
        ArrayList<Wall> testList = new ArrayList<Wall>(10);
        for (int i=0; i < testList.size(); i++){
            testList.get(i).setEdge1(new Point(r.nextDouble(),r.nextDouble(),r.nextDouble()));
            testList.get(i).setEdge2(new Point(r.nextDouble(),r.nextDouble(),r.nextDouble()));
        }
        //this testlist is what needs to be sent over

    }
}
