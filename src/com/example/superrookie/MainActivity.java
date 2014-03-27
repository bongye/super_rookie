package com.example.superrookie;

import com.example.superrookie.bluetooth.BluetoothService;
import com.example.superrookie.bluetooth.DeviceListActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "Main";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

	private boolean detectEnabled;

	private TextView textViewDetectState;
	private Button buttonToggleDetect;
	private Button buttonScan;
	private Button buttonExit;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textViewDetectState = (TextView)findViewById(R.id.textViewDetectState);

		buttonToggleDetect = (Button)findViewById(R.id.buttonDetectToggle);
		buttonToggleDetect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setDetectEnabled(!detectEnabled);
			}
		});

		buttonScan = (Button)findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			}
		});

		buttonExit = (Button)findViewById(R.id.buttonExit);
		buttonExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setDetectEnabled(false);
				MainActivity.this.finish();
			}
		});

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.i(TAG, "onStart");

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			mBluetoothService = new BluetoothService(this, mHandler);
		}
	}
	private void setDetectEnabled(boolean enable) {
		detectEnabled = enable;

		Intent intent = new Intent(this, CallDetectService.class);
		Resources resources = getResources();
		String buttonText;
		String textViewText;
		if (enable) {
			startService(intent);

			buttonText = resources.getString(R.string.turn_off);
			textViewText = resources.getString(R.string.detecting);
		} else {
			stopService(intent);

			buttonText = resources.getString(R.string.turn_on);
			textViewText = resources.getString(R.string.not_detecting);
		}

		buttonToggleDetect.setText(buttonText);
		textViewDetectState.setText(textViewText);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D) Log.d(TAG, "onActivityResult " + resultCode);

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				mBluetoothService = new BluetoothService(this, mHandler);
			} else {
				if (D) Log.d(TAG, "BT is not enabled");
				Toast.makeText(this, "BT is not enabled", Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
	}

	private void connectDevice(Intent data) {
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothService.connect(device);
	}

	private final Handler mHandler = new Handler () {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MESSAGE_DEVICE_NAME:
				break;
			case MESSAGE_STATE_CHANGE:
				break;
			case MESSAGE_READ:
			case MESSAGE_WRITE:
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
}
