package com.example.superrookie;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CallDetectService extends Service {
	private CallHelper callHelper;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		callHelper = new CallHelper(this);

		int result = super.onStartCommand(intent, flags, startId);
		callHelper.start();

		return result;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		callHelper.stop();
	}
}

