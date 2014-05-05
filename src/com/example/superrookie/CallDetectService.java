package com.example.superrookie;

import com.example.superrookie.bluetooth.BluetoothService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class CallDetectService extends Service {
	public static final String TAG = "CallDetectService";
	public static final String EXTRA_DEVICE_ADDRESS = "ADDRESS";
	
	public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    public static final String TOAST = "toast";
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;
	
	private CallHelper callHelper;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart");
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			Log.e(TAG, "BluetoothAdapter is null");
			return;
		}
		mBluetoothService = new BluetoothService(this);
		mBluetoothService.start();
		
		Bundle bundle = intent.getExtras();
		String address = bundle.getString(EXTRA_DEVICE_ADDRESS);
		Log.i(TAG, "Try to connect to a device " + address);
		
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothService.connect(device);
		
		callHelper = new CallHelper(this, mBluetoothService);
		callHelper.start();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();

		callHelper.stop();
		if(mBluetoothService != null) mBluetoothService.stop();
	}
}

