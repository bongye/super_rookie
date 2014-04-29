package com.example.superrookie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallHelper {
	private Context context;
	private TelephonyManager telephonyManager;
	private CallStateListener callStateListener;
	private OutgoingReceiver outgoingReceiver;

	private class CallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				Toast.makeText(context, "Incoming : " + incomingNumber, Toast.LENGTH_LONG).show();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Toast.makeText(context,  "Call end :)", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	private class OutgoingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Toast.makeText(context, "Outgoing: " + number, Toast.LENGTH_LONG).show();
		}
	}

	public CallHelper(Context context) {
		this.context = context;

		callStateListener = new CallStateListener();
		outgoingReceiver = new OutgoingReceiver();
	}

	public void start() {
		telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		context.registerReceiver(outgoingReceiver, intentFilter);
	}

	public void stop() {
		telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		context.unregisterReceiver(outgoingReceiver);
	}
}
