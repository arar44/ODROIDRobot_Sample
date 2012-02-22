package com.hardkernel.android.ODROIDRobot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hardkernel.android.bluetooth.BMP180;
import com.hardkernel.android.bluetooth.BluetoothMotorControlService;

public class ODROIDRobotActivity extends Activity {
	private final static String TAG = "ODROID";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final byte SEND_LETTER_I2C                    = 'i';
	public static final byte SEND_LETTER_BMP180                 = 'b';
	public static final byte SEND_LETTER_FIRMWARE_VERSION       = 'v';
    public static final byte SEND_LETTER_CONSTANT               = 'c';
    public static final byte SEND_LETTER_TEMPERATURE            = 't';
    public static final byte SEND_LETTER_PRESSURE               = 'p';
    public static final byte SEND_LETTER_BATT                   = 'b';
    public static final byte SEND_LETTER_MOTOR                  = 'm';
	
    public static final byte RECEIVED_LETTER_BATT               = 'B';
	public static final byte RECEIVED_LETTER_I2C                = 'I';
	public static final byte RECEIVED_LETTER_BMP180             = 'B';
	public static final byte RECEIVED_LETTER_PRESSURE           = 'P';
    public static final byte RECEIVED_LETTER_TEMPERATURE        = 'T';
    public static final byte RECEIVED_LETTER_CALIBRATIONDATA    = 'C';
    public static final byte RECEIVED_LETTER_FIRMWARE_VERSION   = 'V';
	public static final byte RECEIVED_LETTER_GPIO_CONFIG        = 'G';
	
	private byte [] mBuf = new byte[22];
    private int mIndex = 0;
    private byte [] commandPacket = new byte[5];

	// message type
	public String mMessageType;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	boolean speech_on = false;
	// Layout Views
	private TextView mTitle;
	
	private BMP180 mPressureSensor;
	
	private Timer mRefreshTimer;
    private RefreshTask mRefreshTask;
    private Handler mTimerHandler;
    
	private final static String ROBOT_BT_ADDRESS  = "MACAddress";
	
	private String mMACAddress;
    private SharedPreferences settings;
    
    //For alarm
    private boolean alarm_temperature_max = true;
    private boolean alarm_temperature_min = true;
    private boolean alarm_altitude = true;
    private boolean alarm_pressure = true;
    
    //For SQlite
    private DBAdapter db;

    class RefreshTask extends TimerTask {
        private int count = 0;
        public void run() {
            mTimerHandler.sendEmptyMessage(count);
            count++;
			if (count == 21)
                count = 0;
        }
    }

    private void startTimer() {
        Log.e(TAG, "startTimer");
        mRefreshTimer = new Timer();
        mRefreshTask = new RefreshTask();
		mRefreshTimer.schedule(mRefreshTask, 0, 40);
    }

    private void stopTimer() {
        if (mRefreshTimer != null) {
            mRefreshTimer.cancel();
            mRefreshTimer.purge();
        }
    }

	// Name of the connected device
	private String mConnectedDeviceName = null;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothMotorControlService mCmdSendService = null;
	private boolean bluetoothPaired = false;

	// Buttons menu
	private final static int MENU_ITEM_CONNECT = Menu.FIRST;
	private final static int MENU_ITEM_DISCONNECT = Menu.FIRST + 1;
	private final static int MENU_ITEM_SPEECH = Menu.FIRST + 2;
	private final static int MENU_ITEM_SPEECH_CANCEL = Menu.FIRST + 3;
	private final static int MENU_ITEM_VOICE_COMMANDS = Menu.FIRST + 4;
	private final static int MENU_ITEM_SETTINGS = Menu.FIRST + 5;
	private final static int MENU_ITEM_LAST_COMMANDS = Menu.FIRST + 6;
	private final static int MENU_ITEM_EXIT = Menu.FIRST + 7;
	private final static int MENU_ITEM_ACCELEROMETER = Menu.FIRST + 8;
	private final static int MENU_ITEM_ACCELEROMETER_CANCEL = Menu.FIRST + 9;

	// Speech reocgnizer
	private static final int RECOGNIZER = 1001;
	
	private int mY_axis = 0;
	private int mX_axis = 0;
	private boolean mShaking = false;
	private boolean stopped = true;
	private boolean use_accelerometer = false;
	
	private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
	private float x, y, z;
	private int shaking_count = 0;
    private static final int SHAKE_THRESHOLD = 800;
   
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;
    
	private SensorManager sensorManager;
	private Sensor mSensor;
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	
	private enum Direction {
		N,
		NNE,
		NE,
		NEE,
		E,
		SEE,
		SE,
		SSE,
		S,
		SSW,
		SW,
		SWW,
		W,
		NWW,
		NW,
		NNW,
	}
	
	private Direction mDirection;
	
	private final static int LEFT_WHEEL = 2;
	private final static int RIGHT_WHEEL = 1;
	
	SensorEventListener sel = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
   
            if (gabOfTime > 100) {
                lastTime = currentTime;
   
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];
   
                speed = Math.abs(x + y + z - lastX - lastY - lastZ) /
                        gabOfTime * 10000;
   
                //if (speed > SHAKE_THRESHOLD) {
                if (lastZ < 0 && (z - lastZ) > 11) {
                	Log.e(TAG, "SHAKE");
                	if (!mShaking)
                		shaking_count++;
                		if (shaking_count > 1) {
                			shaking_count = 0;
                			mShaking = true;
                		}
                	else {
                		mShaking = false;
                		stop();
                	}
                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
            
            if (mShaking) {
            	
            	float[] values = event.values;
				int x_axis = (int) values[0] * 2;
				if (x_axis <= -10)
					x_axis = -10;
				else if (x_axis >= 10)
					x_axis =  10;
				
				int y_axis = (int) values[1] * 2;
				if (y_axis <= -10)
					y_axis = -10;
				else if (y_axis >= 10)
					y_axis = 10;
				if ((x_axis > -2 && x_axis < 2) && (y_axis > -2 && y_axis < 2)) {
					if (!stopped) {
						stop();
						stopped = true;
					}
				} else {
					stopped = false;
					if (mX_axis != x_axis || mY_axis != y_axis) {
						mX_axis = x_axis;
						mY_axis = y_axis;
						move(x_axis, y_axis);
					}
				}
			}
		}
    };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mPressureSensor = new BMP180();
		
		ProgressBar progressBar;

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setMax(130);

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		mTimerHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					break;
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					requestTemperature();
					break;
				case 6:
					break;
				case 7:
					break;
				case 8:
					break;
				case 9:
					break;
				case 10:
					break;
				case 11:
					requestBattery();
					break;
				case 12:
					break;
				case 13:
					break;
				case 14:
					break;
				case 15:
					break;
				case 16:
					break;
				case 17:
					break;
				case 18:
					break;
				case 19:
					requestPressure();
					break;
				case 20:
					break;
				}
			}
		};
        
        sensorManager = (SensorManager)getBaseContext().getSystemService(Context.SENSOR_SERVICE);
        
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		pm= (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
		wl= pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
		
		//Change needed for the alarm.
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		mMACAddress = settings.getString(ROBOT_BT_ADDRESS, "");
		
		//To update the system software version.
		PackageInfo packageInfo;
		String swVersion = "";
 	   	try {
 	   		packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
     	    swVersion = packageInfo.versionName;
 	   	} catch (NameNotFoundException e) {
 	   		// TODO Auto-generated catch block
 	   		e.printStackTrace();
 	   	} 
		TextView tv = (TextView)findViewById(R.id.textView_sw_version);
        tv.setText(getString(R.string.software_version) + swVersion);
        
        //Link to HardKernel
        TextView tv_hk = (TextView)findViewById(R.id.textView_hardkernel);
        tv_hk.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hardkernel.com")));
        	}
        });
        
        //For SQlite
        db = new DBAdapter(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mCmdSendService == null)
				setupBT();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mCmdSendService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mCmdSendService.getState() == BluetoothMotorControlService.STATE_NONE) {
				// Start the Bluetooth chat services
				mCmdSendService.start();
			}
		}
		
		wl.acquire();
	}

	public void setMotorValue(int id, int value) {
		sendMessageToMotor(id, value);
		
		if (id == LEFT_WHEEL)
			sendMessageToMotor(4, value);
		if (id == RIGHT_WHEEL)
			sendMessageToMotor(3, value);
		sendMessageToMotor(5, value);
	}

	private void setupBT() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mCmdSendService = new BluetoothMotorControlService(this, mHandler);
		mCmdSendService.setIndexOfMessages(MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_DEVICE_NAME, MESSAGE_TOAST);
		mCmdSendService.setDeviceNameString(DEVICE_NAME);
		mCmdSendService.setToastString(TOAST);
		
		if (mMACAddress.length() != 0) {
			Log.e(TAG, "MAC address " + mMACAddress);
			BluetoothDevice device = mBluetoothAdapter
			.getRemoteDevice(mMACAddress);
			mCmdSendService.connect(device);
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		
		sensorManager.unregisterListener(sel);
		wl.release();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Stop the Bluetooth chat services
		/*if (mCmdSendService != null)
			mCmdSendService.stop();*/
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mCmdSendService != null)
			mCmdSendService.stop();
		stopTimer();
		Alarm.stopTemperature(getApplicationContext());
		Alarm.stopAltitude(getApplicationContext());
		Alarm.stopPressure(getApplicationContext());
	}
	
	private void stop() {
		//Log.e(TAG, "stop");
		sendMessageToMotor(LEFT_WHEEL, 0);
		sendMessageToMotor(RIGHT_WHEEL, 0);
	}
	
	private void move(int x_axis, int y_axis) {
		if (y_axis < 0) { //North
			if (x_axis > 0) { //West
				if (Math.abs(x_axis) < 2) {
					//Log.e(TAG, "North");
					mDirection = Direction.N;
				} else if (Math.abs(y_axis) < 2) {
					//Log.e(TAG, "West");
					mDirection = Direction.W;
				} else if (Math.abs(y_axis) == Math.abs(x_axis)) {
					//Log.e(TAG, "NW");
					mDirection = Direction.NW;
				} else if (Math.abs(y_axis) > Math.abs(x_axis)) {
					//Log.e(TAG, "NNW");
					mDirection = Direction.NNW;
				} else {
					//Log.e(TAG, "NWW");
					mDirection = Direction.NWW;
				}
			} else { //East
				if (Math.abs(x_axis) < 2) {
					//Log.e(TAG, "North");
					mDirection = Direction.N;
				} else if (Math.abs(y_axis) < 2) {
					//Log.e(TAG, "East");
					mDirection = Direction.E;
				} else if (Math.abs(y_axis) == Math.abs(x_axis)) {
					//Log.e(TAG, "NE");
					mDirection = Direction.NE;
				} else if (Math.abs(y_axis) > Math.abs(x_axis)) {
					//Log.e(TAG, "NNE");
					mDirection = Direction.NNE;
				} else {
					//Log.e(TAG, "NEE");
					mDirection = Direction.NEE;
				}
			}
		} else { //South
			if (x_axis > 0) { //West
				if (Math.abs(x_axis) < 2) {
					//Log.e(TAG, "South");
					mDirection = Direction.S;
				} else if (Math.abs(y_axis) < 2) {
					//Log.e(TAG, "West");
					mDirection = Direction.W;
				} else if (Math.abs(y_axis) == Math.abs(x_axis)) {
					//Log.e(TAG, "SW");
					mDirection = Direction.SW;
				} else if (Math.abs(y_axis) > Math.abs(x_axis)) {
					//Log.e(TAG, "SSW");
					mDirection = Direction.SSW;
				} else {
					//Log.e(TAG, "SWW");
					mDirection = Direction.SWW;
				}
			} else { //East
				if (Math.abs(x_axis) < 2) {
					//Log.e(TAG, "South");
					mDirection = Direction.S;
				} else if (Math.abs(y_axis) < 2) {
					//Log.e(TAG, "East");
					mDirection = Direction.E;
				} else if (Math.abs(y_axis) == Math.abs(x_axis)) {
					//Log.e(TAG, "SE");
					mDirection = Direction.SE;
				} else if (Math.abs(y_axis) > Math.abs(x_axis)) {
					//Log.e(TAG, "SSE");
					mDirection = Direction.SSE;
				} else {
					//Log.e(TAG, "SEE");
					mDirection = Direction.SEE;
				}
			}
		}
		move(mDirection);
	}
	
	private void move(Direction dir) {
		int speed = mY_axis * -1;
		int wheel = mX_axis * -1;
		switch (dir) {
		case N:
		case S:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case E:
			setMotorValue(LEFT_WHEEL, wheel);
			setMotorValue(RIGHT_WHEEL, 0);
			break;
		case NE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed / 2);
			break;
		case NNE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed - 3);
			break;
		case NEE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed / 2 - 3);
			break;
		case NW:
			setMotorValue(LEFT_WHEEL, speed / 2);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case NNW:
			setMotorValue(LEFT_WHEEL, speed - 3);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case NWW:
			setMotorValue(LEFT_WHEEL, speed / 2 - 3);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case W:
			setMotorValue(LEFT_WHEEL, 0);
			setMotorValue(RIGHT_WHEEL, -wheel);
			break;
		case SE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed / 2);
			break;
		case SSE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed + 3);
			break;
		case SEE:
			setMotorValue(LEFT_WHEEL, speed);
			setMotorValue(RIGHT_WHEEL, speed / 2 + 3);
			break;
		case SW:
			setMotorValue(LEFT_WHEEL, speed / 2);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case SSW:
			setMotorValue(LEFT_WHEEL, speed + 3);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		case SWW:
			setMotorValue(LEFT_WHEEL, speed / 2 + 3);
			setMotorValue(RIGHT_WHEEL, speed);
			break;
		}
	}
	
	private void sendMessageToMotor(int id, int value) {
		//Return to the previous version(revision 35), the voice recognition works, with the new code does not work(revision 40).
	   /* Message msg = Message.obtain(mHandler, SEND_LETTER_MOTOR, id, value); 
	    mHandler.sendMessage(msg);
	    */
		//Log.e(TAG, "sendMessageToMotor(" + id + ", " + value + ")");
		if (mCmdSendService.getState() != BluetoothMotorControlService.STATE_CONNECTED) {
            return;
        } 
		
		commandPacket[0] = SEND_LETTER_MOTOR;
		commandPacket[1] = 0;
		commandPacket[2] = (byte) id;
		if (id == 1)
			value *= -1;
		if (value > 0) {
			commandPacket[3] = '-';
			commandPacket[4] = (byte) value;
		} else {
			commandPacket[3] = '+';
			commandPacket[4] = (byte) (value * -1);
		}
		mCmdSendService.write(commandPacket);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

    		short value = 0;

			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothMotorControlService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					mIndex = 0;
                    initI2C();
					bluetoothPaired = true;
					break;
					
				case BluetoothMotorControlService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
					
				case BluetoothMotorControlService.STATE_LISTEN:
				case BluetoothMotorControlService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					stopTimer();
					bluetoothPaired = false;
					break;
				}
				break;
				
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(
						getApplicationContext(),
						getString(R.string.connected_to) + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
				
			case MESSAGE_TOAST:
				if (msg.getData().getString(TOAST).equals("unable connect")) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.unable_connect),
							Toast.LENGTH_SHORT).show();
				} else if (msg.getData().getString(TOAST)
						.equals("connection lost")) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.connection_lost),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
							.show();
				}
				break;
				
			case SEND_LETTER_FIRMWARE_VERSION:
                if (mCmdSendService.getState() != BluetoothMotorControlService.STATE_CONNECTED) {
                    return;
                }    
     
                commandPacket[0] = SEND_LETTER_FIRMWARE_VERSION;
                commandPacket[1] = 0; 
                commandPacket[2] = 0; 
                commandPacket[3] = 0; 
                commandPacket[4] = 0; 
     
                mCmdSendService.write(commandPacket);
                break;

			case SEND_LETTER_MOTOR: 
				if (mCmdSendService.getState() != BluetoothMotorControlService.STATE_CONNECTED) {
                    return;
                }    
     
				value = (short) msg.arg2;
        		commandPacket[0] = SEND_LETTER_MOTOR;
        		commandPacket[1] = 0;
        		commandPacket[2] = (byte) msg.arg1;
        		if (msg.arg1 == 1)
        			value *= -1;
        		if (value > 0) {
        			commandPacket[3] = '-';
        			commandPacket[4] = (byte) value;
        		} else {
        			commandPacket[3] = '+';
        			commandPacket[4] = (byte) (value * -1);
        		}
        		mCmdSendService.write(commandPacket);
        		break;
				
			case SEND_LETTER_BATT:
                if (commandPacket[0] != 0) {
                    return;
                }

                commandPacket[0] = SEND_LETTER_BATT;

                mCmdSendService.write(commandPacket);
                break;
				
			case SEND_LETTER_I2C:

                if (msg.arg1 == SEND_LETTER_I2C) {
                    commandPacket[0] = SEND_LETTER_I2C;
                    commandPacket[1] = SEND_LETTER_I2C;
                }

                if (msg.arg1 == SEND_LETTER_BMP180) {
                    commandPacket[0] = SEND_LETTER_I2C;
                    commandPacket[1] = SEND_LETTER_BMP180;
                }

                if (msg.arg1 == SEND_LETTER_CONSTANT) {
                    commandPacket[0] = SEND_LETTER_I2C;
                    commandPacket[1] = SEND_LETTER_CONSTANT;
                    commandPacket[2] = 0x00;
                    commandPacket[3] = (byte) msg.arg2;
                }

                if (msg.arg1 == SEND_LETTER_TEMPERATURE) {
                    if (commandPacket[0] != 0) {
                        //Log.e(TAG, "returned");
                        return;
                    }
                    commandPacket[0] = SEND_LETTER_I2C;
                    commandPacket[1] = SEND_LETTER_TEMPERATURE;
                }

                if (msg.arg1 == SEND_LETTER_PRESSURE) {
                    if (commandPacket[0] != 0) {
                        //Log.e(TAG, "returned");
                        return;
                    }
                    commandPacket[0] = SEND_LETTER_I2C;
                    commandPacket[1] = SEND_LETTER_PRESSURE;
                }

                mCmdSendService.write(commandPacket);
                break;
                
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;

                if (readBuf[0] == commandPacket[0] + ('A' - 'a')) {
					commandPacket[0] = 0;
				}

				switch(readBuf[0]) {
    			case RECEIVED_LETTER_BATT:
                {
                    value = (short) (readBuf[3] << 8);
                    value |= (short) (readBuf[4] & 0x00ff);
                    updateBattery(value);
                }
                    break;

                case RECEIVED_LETTER_FIRMWARE_VERSION: 
				{	
					value = (short) (readBuf[1] << 8);
					value |= (short) readBuf[2];
					short major = value;
					value = (short) (readBuf[3] << 8);
					value |= (short) readBuf[4];
					short minior = value;
					
					updateVersion(major, minior);
				}
					break;
					
				case RECEIVED_LETTER_I2C:
					if (readBuf[1] == RECEIVED_LETTER_I2C) {
						initBMP180();
					} else if (readBuf[1] == RECEIVED_LETTER_BMP180) {
						getCalibrationData(mPressureSensor.getRegisterAddress());
	                } else if (readBuf[1] == RECEIVED_LETTER_TEMPERATURE) {
	                    ByteBuffer bb = ByteBuffer.allocate(4);
	                    bb.order(ByteOrder.LITTLE_ENDIAN);
	                    bb.put(readBuf[2]);
	                    bb.put(readBuf[3]);
	                    bb.rewind();
	                    if (mPressureSensor.isAvailable()) {
	                        long temperature = mPressureSensor.calculateTrueTemperature(bb.getInt());
	                        updateTemperature(temperature);
	                    }
	                } else if (readBuf[1] == RECEIVED_LETTER_PRESSURE) {
	                    ByteBuffer bb = ByteBuffer.allocate(4);
	                    bb.order(ByteOrder.LITTLE_ENDIAN);
	                    bb.put(readBuf[2]);
	                    bb.put(readBuf[3]);
	                    bb.rewind();
	                    long pressure = 0;
	                    if (mPressureSensor.isAvailable()) {
	                        pressure = mPressureSensor.calculateTruePressure(bb.getInt());
	                        updatePressure(pressure);
	                    }
	                    long altitude = (long) mPressureSensor.calculateAltitude(pressure);
	                    updateAltitude(altitude);
	                } else if (readBuf[1] == RECEIVED_LETTER_CALIBRATIONDATA) {
	                    if (updateCalibrationData(readBuf)) {
	                        mPressureSensor.setNextRegisterAddress();
	                        getCalibrationData(mPressureSensor.getRegisterAddress());
	                    } else
	                    	sensorManager.registerListener(sel, mSensor,SensorManager.SENSOR_DELAY_GAME);
	                }
				    break;
			    }
            default:
                break;
            }
		}
	};

    private void initDevice() {
    	Log.e(TAG, "initDevice");
    	initI2C();
    	initBMP180();
		getCalibrationData(mPressureSensor.getRegisterAddress());
    }
 
	private void getCalibrationData(byte addr) {
        Message msg = Message.obtain(mHandler, SEND_LETTER_I2C, SEND_LETTER_CONSTANT, addr);
        mHandler.sendMessage(msg);
    }

	private void initI2C() {
        Log.e(TAG, "initI2C");
        Message msg = Message.obtain(mHandler, SEND_LETTER_I2C, SEND_LETTER_I2C, 0);
        mHandler.sendMessage(msg);
    }

    private void initBMP180() {
        Log.e(TAG, "initBMP180");
        Message msg = Message.obtain(mHandler, SEND_LETTER_I2C, SEND_LETTER_BMP180, 0);
        mHandler.sendMessage(msg);
    }
    
    private void requestVersion() {
        Log.e(TAG, "getVersion");
        Message msg = Message.obtain(mHandler, SEND_LETTER_FIRMWARE_VERSION, 0, 0);
        mHandler.sendMessage(msg);
    }
    
    private void requestBattery() {
        //Log.e(TAG, "getBattery");
        Message msg = Message.obtain(mHandler, SEND_LETTER_BATT, 0, 0);
        mHandler.sendMessage(msg);
    }

    private void requestTemperature() {
        //Log.e(TAG, "getTemperature");
        Message msg = Message.obtain(mHandler, SEND_LETTER_I2C, SEND_LETTER_TEMPERATURE, 0);
        mHandler.sendMessage(msg);
    }

    private void requestPressure() {
        //Log.e(TAG, "getPressure");
        Message msg = Message.obtain(mHandler, SEND_LETTER_I2C, SEND_LETTER_PRESSURE, 0);
        mHandler.sendMessage(msg);
    }
    
    private void updateVersion(short major, short minior) {
        TextView tv = (TextView)findViewById(R.id.textView_fw_version);
        tv.setText(getString(R.string.firmware_version) + major + "." + minior);
        Log.e(TAG, "Major : " + major);
        Log.e(TAG, "Minior : " + minior);
    }

	private void updateBattery(short value) {
    	//Log.e(TAG, "updateBattery = " + value);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setProgress(value - 520);
    }

    private void updateTemperature(long value) {
    	//Log.e(TAG, "updateTemperature = " + value);
    	if ((value * 0.1) > Integer.parseInt(settings.getString("temperature_max", "500")) &&
    			settings.getBoolean("temperature_alarm", false)){
    		if(alarm_temperature_max){
    			Alarm.playTemperature(getApplicationContext(), R.raw.alarm);
    			alarm_temperature_max = false;
    		}    			
    	}else if((value * 0.1) < Integer.parseInt(settings.getString("temperature_min", "-50")) &&
    			settings.getBoolean("temperature_alarm", false)){
    		if(alarm_temperature_min){
    			Alarm.playTemperature(getApplicationContext(), R.raw.alarm);
    			alarm_temperature_min = false;
    		} 
    	}else{
    		Alarm.stopTemperature(getApplicationContext());
    		alarm_temperature_max = true;
    		alarm_temperature_min = true;
    	}
        TextView tv_c = (TextView)findViewById(R.id.textView_temperature_c);
        tv_c.setText(String.format("%.1f", value * 0.1));
        TextView tv_f = (TextView)findViewById(R.id.textView_temperature_f);
        tv_f.setText(String.format("%.1f", (value * 0.18 + 32)));
    }

    private void updatePressure(long value) {
    	//Log.e(TAG, "updatePressure = " + value);
    	if ((value * 0.01) > Integer.parseInt(settings.getString("pressure_max", "1200")) &&
    			settings.getBoolean("pressure_alarm", false)){
    		if(alarm_pressure){
    			Alarm.playPressure(getApplicationContext(), R.raw.alarm);
    			alarm_pressure = false;
    		}    			
    	}else{
    		Alarm.stopPressure(getApplicationContext());
    		alarm_pressure = true;
    	}
        TextView tv = (TextView)findViewById(R.id.textView_pressure);
        tv.setText(String.format("%.2f", value * 0.01));
    }

    private void updateAltitude(long value) {
    	if (value > Integer.parseInt(settings.getString("altitude_max", "8000")) &&
    			settings.getBoolean("altitude_alarm", false)){
    		if(alarm_altitude){
    			Alarm.playAltitude(getApplicationContext(), R.raw.alarm);
    			alarm_altitude = false;
    		}    			
    	}else{
    		Alarm.stopAltitude(getApplicationContext());
    		alarm_altitude = true;
    	}
        TextView tv = (TextView)findViewById(R.id.textView_altitude);
        tv.setText("" + value);

        tv = (TextView)findViewById(R.id.textView_altitude_feet);

        tv.setText(String.format("%.0f", value * 3.2808399));
    }
    
    private boolean updateCalibrationData(byte [] value) {
        mBuf[mIndex++] = value[2];
        mBuf[mIndex++] = value[3];

        if (mIndex == 22) {
            fillCalibarationData(mBuf);
            requestVersion();
            startTimer();
            return false;
        } else
            return true;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mCmdSendService.connect(device);
				
				SharedPreferences.Editor edit = settings.edit();
				edit.putString(ROBOT_BT_ADDRESS, address);
				edit.commit();
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupBT();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		if (requestCode == RECOGNIZER && resultCode == Activity.RESULT_OK) {
			// returned data is a list of matches to the speech input
			ArrayList<String> result = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			Log.d("SPEECH", "size:" + result.size());
			String datos ;
			for (int i = result.size()-1; i >= 0 ; i--) {
				
				Log.d("SPEECH", "str:"+i+ ":" + result.get(i));

				datos= result.get(i);
				System.out.println(datos);

				mTitle = (TextView) findViewById(R.id.title_left_text);
				mTitle.setText((i+1)+"/"+result.size()+ ":" + result.get(i));

				if (datos.equals("izquierda") || datos.equals("left")
						|| datos.equals("gauche")
						|| datos.equals(getString(R.string.left_korean))) {
					setMotorValue(RIGHT_WHEEL, -1);
					setMotorValue(LEFT_WHEEL, 10);
					try {
						Thread.sleep(1250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(LEFT_WHEEL, 0);
					setMotorValue(RIGHT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					db.close();
					break;
				}
				if (datos.equals("derecha") || datos.equals("right")
						|| datos.equals("droit")
						|| datos.equals(getString(R.string.right_korean))) {
					setMotorValue(RIGHT_WHEEL, 10);
					setMotorValue(LEFT_WHEEL, -1);
					try {
						Thread.sleep(1250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
				if (datos.equals("avanzar") || datos.equals("move")
						|| datos.equals("deplacer")
						|| datos.equals(getString(R.string.move_korean))) {
					setMotorValue(RIGHT_WHEEL, 10);
					setMotorValue(LEFT_WHEEL, 10);
					try {
						Thread.sleep(1100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
				if (datos.equals("girar") || datos.equals("rotate")
						|| datos.equals("tournez")
						|| datos.equals(getString(R.string.rotate_korean))) {
					setMotorValue(RIGHT_WHEEL, -10);
					setMotorValue(LEFT_WHEEL, 10);
					try {
						Thread.sleep(1100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
				if (datos.equals("correr") || datos.equals("run")
						|| datos.equals("courir")
						|| datos.equals(getString(R.string.run_korean))) {
					setMotorValue(RIGHT_WHEEL, 10);
					setMotorValue(LEFT_WHEEL, 10);
					try {
						Thread.sleep(2200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
				if (datos.equals("retroceder") || datos.equals("back")
						|| datos.equals("retour")
						|| datos.equals(getString(R.string.back_korean))) {
					setMotorValue(RIGHT_WHEEL, -10);
					setMotorValue(LEFT_WHEEL, -10);
					try {
						Thread.sleep(1100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
				if (datos.equals("parar") || datos.equals("stop")
						|| datos.equals("stop")
						|| datos.equals(getString(R.string.stop_korean))) {
					setMotorValue(RIGHT_WHEEL, 0);
					setMotorValue(LEFT_WHEEL, 0);
					db.open();
					Long now = Long.valueOf(System.currentTimeMillis());
					Date date = new Date(now);
					db.insertTitle(datos, date.toLocaleString());
					break;
				}
			}
			if (speech_on)
				VoiceSpeech();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_CONNECT, 0, getString(R.string.connect)).setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(0, MENU_ITEM_DISCONNECT, 0, getString(R.string.disconnect))
				.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_ITEM_ACCELEROMETER, 0, getString(R.string.use_accelerometer))
				.setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
		menu.add(0, MENU_ITEM_ACCELEROMETER_CANCEL, 0, getString(R.string.use_accelerometer_cancel))
				.setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
		menu.add(0, MENU_ITEM_SPEECH, 0, getString(R.string.speech)).setIcon(
				R.drawable.speech);
		menu.add(0, MENU_ITEM_SPEECH_CANCEL, 0, getString(R.string.speech_cancel)).setIcon(
				R.drawable.speech);
		menu.add(0, MENU_ITEM_VOICE_COMMANDS, 0,
				getString(R.string.voice_commands)).setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, MENU_ITEM_SETTINGS, 0,
				getString(R.string.settings)).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ITEM_LAST_COMMANDS, 0,
				getString(R.string.command_history)).setIcon(
				android.R.drawable.ic_menu_directions);
		menu.add(0, MENU_ITEM_EXIT, 0, getString(R.string.exit)).setIcon(
				R.drawable.exit);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (bluetoothPaired) {
			menu.findItem(MENU_ITEM_DISCONNECT).setVisible(true);
			menu.findItem(MENU_ITEM_SPEECH).setVisible(true);
			menu.findItem(MENU_ITEM_SPEECH_CANCEL).setVisible(true);
			if(use_accelerometer){
				menu.findItem(MENU_ITEM_ACCELEROMETER_CANCEL).setVisible(true);
				menu.findItem(MENU_ITEM_ACCELEROMETER).setVisible(false);
			}else{
				menu.findItem(MENU_ITEM_ACCELEROMETER_CANCEL).setVisible(false);
				menu.findItem(MENU_ITEM_ACCELEROMETER).setVisible(true);
			}
		} else {
			menu.findItem(MENU_ITEM_DISCONNECT).setVisible(false);
			menu.findItem(MENU_ITEM_SPEECH).setVisible(false);
			menu.findItem(MENU_ITEM_SPEECH_CANCEL).setVisible(false);
			menu.findItem(MENU_ITEM_ACCELEROMETER).setVisible(false);
			menu.findItem(MENU_ITEM_ACCELEROMETER_CANCEL).setVisible(false);
		}
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_CONNECT:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case MENU_ITEM_DISCONNECT:
			// Stop the Bluetooth chat services
			if (mCmdSendService != null)
				mCmdSendService.stop();
			mTitle.setText(R.string.title_not_connected);
			stopTimer();
			return true;
		case MENU_ITEM_SPEECH:
			// Voice Recognition
			VoiceSpeech();
			speech_on = true;
			return true;
		case MENU_ITEM_SPEECH_CANCEL:
			// Voice Recognition
			speech_on = false;
			return true;
		case MENU_ITEM_VOICE_COMMANDS:
			// Voice Commands
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.voice_commands));
			StringBuffer str = new StringBuffer();
			// English
			str.append(
					"Possible voice commands in English are: left, right, move, back, rotate, run and stop.")
					.append('\n').append('\n');
			// Spanish
			str.append(
					"Los comandos de voz posibles en Español son: izquierda, derecha, avanzar, retroceder, girar, correr y parar.")
					.append('\n').append('\n');
			// French
			str.append(
					"Les commandes vocales sont possibles en français: gauche, droit, déplacer, tournez, courir, retour et stop.")
					.append('\n').append('\n');
			// Korean
			str.append("Possible voice commands in Korean are: "
					+ getString(R.string.left_korean) + ", "
					+ getString(R.string.right_korean) + ", "
					+ getString(R.string.move_korean) + ", "
					+ getString(R.string.back_korean) + ", "
					+ getString(R.string.rotate_korean) + ", "
					+ getString(R.string.run_korean) + ", "
					+ getString(R.string.stop_korean));
			builder.setMessage(str);
			builder.setCancelable(true);
			builder.setIcon(android.R.drawable.ic_menu_help);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// nothing
						}
					});
			builder.create().show();
			return true;
		case MENU_ITEM_SETTINGS:
			startActivity(new Intent(getApplicationContext(), Settings.class));
			return true;
		case MENU_ITEM_LAST_COMMANDS:
			startActivity(new Intent(getApplicationContext(), RoutesViewer.class));
			return true;
		case MENU_ITEM_EXIT:
			if (mCmdSendService != null)
				mCmdSendService.stop();
			finish();
			System.exit(0);
			return true;
		case MENU_ITEM_ACCELEROMETER:
			if (bluetoothPaired) {
				mShaking = !mShaking;
				use_accelerometer = true;
			}
			return true;
		case MENU_ITEM_ACCELEROMETER_CANCEL:
			if (bluetoothPaired) {
				use_accelerometer = false;
				mShaking = false;
        		stop();
			}
			return true;
		}
		return false;
	}

	public void VoiceSpeech() {

		try {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH );
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,100);
					
			startActivityForResult(intent, RECOGNIZER);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "No Speech support",
					Toast.LENGTH_LONG).show();
		}

	}
	
    private void fillCalibarationData(byte [] buf) {
        ByteBuffer bb2short = ByteBuffer.allocate(2);
        bb2short.order(ByteOrder.LITTLE_ENDIAN);
        bb2short.put(buf[0]);
        bb2short.put(buf[1]);
        short AC1 = bb2short.getShort(0);
        Log.e(TAG, String.format("AC1 = %d", AC1));

        bb2short.clear();
        bb2short.put(buf[2]);
        bb2short.put(buf[3]);
        short AC2 = bb2short.getShort(0);
        Log.e(TAG, String.format("AC2 = %d", AC2));

        bb2short.clear();
        bb2short.put(buf[4]);
        bb2short.put(buf[5]);
        short AC3 = bb2short.getShort(0);
        Log.e(TAG, String.format("AC3 = %d", AC3));

        ByteBuffer bb2int= ByteBuffer.allocate(4);
        bb2int.order(ByteOrder.LITTLE_ENDIAN);
        bb2int.put(buf[6]);
        bb2int.put(buf[7]);
        bb2int.rewind();
        int AC4 = bb2int.getInt();
        Log.e(TAG, String.format("AC4 = %d", AC4));

        bb2int.clear();
        bb2int.put(buf[8]);
        bb2int.put(buf[9]);
        bb2int.rewind();
        int AC5 = bb2int.getInt();
        Log.e(TAG, String.format("AC5 = %d", AC5));

        bb2int.clear();
        bb2int.put(buf[10]);
        bb2int.put(buf[11]);
        bb2int.rewind();
        int AC6 = bb2int.getInt();
        Log.e(TAG, String.format("AC6 = %d", AC6));

        bb2short.clear();
        bb2short.put(buf[12]);
        bb2short.put(buf[13]);
        short B1 = bb2short.getShort(0);
        Log.e(TAG, String.format("B1 = %d", + B1));

        bb2short.clear();
        bb2short.put(buf[14]);
        bb2short.put(buf[15]);
        short B2 = bb2short.getShort(0);
        Log.e(TAG, String.format("B2 = %d", B2));

        bb2short.clear();
        bb2short.put(buf[16]);
        bb2short.put(buf[17]);
        short MB = bb2short.getShort(0);
        Log.e(TAG, String.format("MB = %d", MB));
        
        bb2short.clear();
        bb2short.put(buf[18]);
        bb2short.put(buf[19]);
        short MC = bb2short.getShort(0);
        Log.e(TAG, String.format("MC = %d", MC));
        
        bb2short.clear();
        bb2short.put(buf[20]);
        bb2short.put(buf[21]);
        short MD = bb2short.getShort(0);
        Log.e(TAG, String.format("MD = %d", MD));
        
        mPressureSensor.setCalibrationData(AC1, AC2, AC3, AC4, AC5, AC6, B1, B2, MB, MC, MD);
    }
}
