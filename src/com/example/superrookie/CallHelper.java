package com.example.superrookie;

import com.example.superrookie.bluetooth.BluetoothService;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CallHelper {
	private static final String TAG = "CallHelper";
	
	public static final int CALL_START = 0;
	public static final int CALL_END = 1;
	
	private Context context;
	private BluetoothService mBluetoothService;
	private TelephonyManager telephonyManager;
	private CallStateListener callStateListener;

	private class CallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				Log.i(TAG, "Call start signal received(incoming).");
				Toast.makeText(context, "Incoming : " + incomingNumber, Toast.LENGTH_LONG).show();
				sendMessage("0");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.i(TAG, "Call start signal received(outgoing).");
				Toast.makeText(context,  "Outgoing", Toast.LENGTH_LONG).show();
				sendMessage("0");
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				Log.i(TAG, "Call end signal received.");
				Toast.makeText(context, "End", Toast.LENGTH_LONG).show();
				sendMessage("1");
				break;
			}
		}
	}

	public CallHelper(Context context, BluetoothService bluetoothService) {
		this.context = context;
		this.mBluetoothService = bluetoothService;

		callStateListener = new CallStateListener();
	}

	public void start() {
		telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	public void stop() {
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
	}
	
	public void sendMessage(String msg) {
		if(mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			Log.d(TAG, "Bluetooth is not connected.");
		}
		
		Log.i(TAG, "Send " + msg);
		byte[] bytes = msg.getBytes();
		mBluetoothService.write(bytes);
	}
}
