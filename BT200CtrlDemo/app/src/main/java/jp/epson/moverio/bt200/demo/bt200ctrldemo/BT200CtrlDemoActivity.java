package jp.epson.moverio.bt200.demo.bt200ctrldemo;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class BT200CtrlDemoActivity extends Activity {
	private String TAG = "Bt2CtrlDemoActivity";
	private MapDrawable lineDrawable = new MapDrawable();
	private ImageView mapView;
	//private Button updateButton;
	private Button rotButton;
	private Button moveLeft;
	private Button moveRight;
	private Button moveUp;
	private Button moveDown;
	private CheckBox mapMode;
	private int count = 0;
	private int rotationDegree = 0;



	/*
	private ToggleButton mToggleButton_2d3d = null;
	private Button mButton_dmute = null;
	private SeekBar mSeekBar_backlight = null;
	private ToggleButton mToggleButton_amute = null;
	private ToggleButton mToggleButton_sensor = null;


	private DisplayControl mDisplayControl = null;
	private AudioControl mAudioControl = null;
	private SensorControl mSensorControl = null;
	*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bt2_ctrl_demo);

		getActionBar().hide();

		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		//winParams.flags |= WindowManager.LayoutParams.FLAG_SMARTFULLSCREEN;
		winParams.flags |= 0x80000000;
		win.setAttributes(winParams);

		//Pregenerated Wall
		Wall walls[] = new Wall[8];
		Coordinate A = new Coordinate(90,400);
		Coordinate B = new Coordinate(175,400);
		Coordinate C = new Coordinate(90,140);
		Coordinate D = new Coordinate(175,190);
		Coordinate E = new Coordinate(10,140);
		Coordinate F = new Coordinate(270,190);
		Coordinate G = new Coordinate(10,90);
		Coordinate H = new Coordinate(90,90);
		Coordinate I = new Coordinate(90,40);
		Coordinate J = new Coordinate(270,40);
		walls[0] = new Wall(A,C);
		walls[1] = new Wall(C,E);
		walls[2] = new Wall(G,H);
		walls[3] = new Wall(H,I);
		walls[4] = new Wall(I,J);
		walls[5] = new Wall(J,F);
		walls[6] = new Wall(F,D);
		walls[7] = new Wall(D,B);


		lineDrawable.setWallArray(walls);
		mapView = (ImageView) findViewById(R.id.mapView);
		moveLeft = (Button)findViewById(R.id.leftButton);
		moveRight = (Button)findViewById(R.id.rightButton);
		moveUp = (Button)findViewById(R.id.upButton);
		moveDown = (Button)findViewById(R.id.downButton);
		mapMode = (CheckBox) findViewById(R.id.modeBox);
		rotButton = (Button)findViewById(R.id.rotateButton);
		mapView.setImageDrawable(lineDrawable);

		moveUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapView.invalidate();
				lineDrawable.moveY+= 20;
			}
		});

		moveDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapView.invalidate();
				lineDrawable.moveY-= 20;
			}
		});

		moveLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapView.invalidate();
				lineDrawable.moveX+=20;
			}
		});

		moveRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapView.invalidate();
				lineDrawable.moveX-=20;
			}
		});

		mapMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapView.invalidate();
				if(((CheckBox)view).isChecked()){
					lineDrawable.setMapMode(true);
				}
				else {
					lineDrawable.setMapMode(false);
				}
				mapView.setImageDrawable(lineDrawable);
			}
		});

		rotButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rotationDegree = (rotationDegree+45)%360;
				Wall w[] = lineDrawable.getWallArray();
				mapView.invalidate();
				lineDrawable.setDegreeRotation(rotationDegree);
				lineDrawable.setWallArray(w);
			}
		});


		/*updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapView.invalidate();
				if(count%2 == 0){
					Wall[] newWalls = new Wall[3];
					for (int i=0; i < 3; i++){
						Random r = new Random();
						newWalls[i] = new Wall();
						newWalls[i].setCoordinates(r.nextInt(300),r.nextInt(300),r.nextInt(300),r.nextInt(300));
					}
					rotationDegree = 0;
					lineDrawable.setDegreeRotation(rotationDegree);
					lineDrawable.setWallArray(newWalls);
				}
				else {
					Wall[] oddWalls = new Wall[4];
					for (int i=0; i < 4; i++){
						Random r = new Random();
						oddWalls[i] = new Wall();
						oddWalls[i].setCoordinates(r.nextInt(300),r.nextInt(300),r.nextInt(300),r.nextInt(300));
					}
					rotationDegree = 0;
					lineDrawable.setDegreeRotation(rotationDegree);
					lineDrawable.setWallArray(oddWalls);
				}
				count++;
			}
		});*/



		/*mDisplayControl = new DisplayControl(this);
		mAudioControl = new AudioControl(this);
		mSensorControl = new SensorControl(this);
		// 2D/3D�ϊ�
		mToggleButton_2d3d = (ToggleButton)findViewById(R.id.toggleButton_2d3d);
		mToggleButton_2d3d.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
				if(arg1){
					Log.d(TAG,"set 3D display mode.");
					mDisplayControl.setMode(DisplayControl.DISPLAY_MODE_3D, true);
				}
				else{
					Log.d(TAG,"set 2D display mode.");
					mDisplayControl.setMode(DisplayControl.DISPLAY_MODE_2D, false);
				}
			}
	    });
	    // �f�B�X�v���C�̃~���[�g�̐ݒ�ύX
		mButton_dmute = (Button)findViewById(R.id.Button_dmute);
		mButton_dmute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Set LCD mute ON. (display OFF)");
				mDisplayControl.setMute(true);
			    try{
			    	Thread.sleep(3000); //3000�~���bSleep����
			    }catch(InterruptedException e){}

				Log.d(TAG, "Set LCD mute OFF. (display ON)");
				mDisplayControl.setMute(false);
			}
		});
	    // ���邳�̐ݒ�ύX
		mSeekBar_backlight = (SeekBar)findViewById(R.id.seekBar_backlight);
		mSeekBar_backlight.setMax(20);
		mSeekBar_backlight.setProgress(mDisplayControl.getBacklight());
		mSeekBar_backlight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Set LCD back-light level:"+progress);
				mDisplayControl.setBacklight(progress);
			}
		});
		
		// �I�[�f�B�I�̃~���[�g�̐ݒ�ύX
		mToggleButton_amute = (ToggleButton)findViewById(R.id.toggleButton_amute);
		mToggleButton_amute.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
				if(arg1){
					Log.d(TAG,"Set audio mute ON.");
					mAudioControl.setMute(true);
				}
				else{
					Log.d(TAG,"set audio mute OFF.");
					mAudioControl.setMute(false);
				}
			}
	    });
		
		// �Z���T�[�̐؂�ւ�
		mToggleButton_sensor = (ToggleButton)findViewById(R.id.toggleButton_sensor);
		if(SensorControl.SENSOR_MODE_CONTROLLER == mSensorControl.getMode()){
			mToggleButton_sensor.setChecked(true);
		}
		else{
			mToggleButton_sensor.setChecked(false);
		}
		mToggleButton_sensor.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){
					Log.d(TAG,"set sensor of controller.");
					mSensorControl.setMode(SensorControl.SENSOR_MODE_CONTROLLER);
				}
				else{
					Log.d(TAG,"set sensor of headset.");
					mSensorControl.setMode(SensorControl.SENSOR_MODE_HEADSET);
				}
			}
		});*/

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.bt2_ctrl_demo, menu);
		return true;
	}

}
