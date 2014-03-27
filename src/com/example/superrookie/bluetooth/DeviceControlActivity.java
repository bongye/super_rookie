package com.example.superrookie.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.superrookie.R;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristic =
    	new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// TODO Auto-generated method stub
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED.equals(action)) {
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	private final ExpandableListView.OnChildClickListener serviceListClickListener =
		new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				if (mGattCharacteristic != null) {
					final BluetoothGattCharacteristic characteristic =
						mGattCharacteristic.get(groupPosition).get(childPosition);
					final int charaProp = characteristic.getProperties();
					if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
						if (mNotifyCharacteristic != null) {
							mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
							mNotifyCharacteristic = null;
						}
						mBluetoothLeService.readCharacteristic(characteristic);
					}
					if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
						mNotifyCharacteristic = characteristic;
						mBluetoothLeService.setCharacteristicNotification(characteristic, true);
					}
					return true;
				}
				return false;
			}
		};

	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.gatt_services_characteristics);

    	final Intent intent = getIntent();
    	mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
    	mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

    	((TextView)findViewById(R.id.device_address)).setText(mDeviceAddress);
    	mGattServicesList = (ExpandableListView)findViewById(R.id.gatt_services_list);
    	mGattServicesList.setOnChildClickListener(serviceListClickListener);
    	mConnectionState = (TextView)findViewById(R.id.connection_state);
    	mDataField = (TextView)findViewById(R.id.data_value);

    	getActionBar().setTitle(mDeviceName);
    	getActionBar().setDisplayHomeAsUpEnabled(true);
    	Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
    	bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    	if (mBluetoothLeService != null) {
    		final boolean result = mBluetoothLeService.connect(mDeviceAddress);
    		Log.d(TAG, "Connect request result = " + result);
    	}
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unbindService(mServiceConnection);
    	mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.gatt_services, menu);
    	if (mConnected) {
    		menu.findItem(R.id.menu_connect).setVisible(false);
    		menu.findItem(R.id.menu_disconnect).setVisible(true);
    	} else {
    		menu.findItem(R.id.menu_connect).setVisible(true);
    		menu.findItem(R.id.menu_disconnect).setVisible(false);
    	}
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_connect :
    		mBluetoothLeService.connect(mDeviceAddress);
    		return true;
    	case R.id.menu_disconnect:
    		mBluetoothLeService.disconnect();
    		return true;
    	case android.R.id.home:
    		onBackPressed();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mConnectionState.setText(resourceId);
			}
		});
    }

    private void displayData(String data) {
    	if (data != null) {
    		mDataField.setText(data);
    	}
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
    	if (gattServices == null) return;
    	String uuid = null;
    	String unknownServiceString = getResources().getString(R.string.unknown_service);
    	String unknownCharacteristic = getResources().getString(R.string.unknown_characteristic);
    	ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
    	ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData =
    			new ArrayList<ArrayList<HashMap<String,String>>>();
    	mGattCharacteristic = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    	for (BluetoothGattService gattService : gattServices) {
    		HashMap<String, String> currentServiceData = new HashMap<String, String>();
    		uuid = gattService.getUuid().toString();
    		currentServiceData.put(LIST_NAME, unknownServiceString);
    		currentServiceData.put(LIST_UUID, uuid);
    		gattServiceData.add(currentServiceData);

    		ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
    				new ArrayList<HashMap<String,String>>();
    		List<BluetoothGattCharacteristic> gattCharacteristics =
    				gattService.getCharacteristics();
    		ArrayList<BluetoothGattCharacteristic> charas =
    				new ArrayList<BluetoothGattCharacteristic>();

    		for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
    			charas.add(gattCharacteristic);
    			HashMap<String, String> currentCharasData = new HashMap<String, String>();
    			uuid = gattCharacteristic.getUuid().toString();
    			currentCharasData.put(LIST_NAME, unknownCharacteristic);
    			currentCharasData.put(LIST_UUID, uuid);
    			gattCharacteristicGroupData.add(currentCharasData);
    		}
    		mGattCharacteristic.add(charas);
    		gattCharacteristicData.add(gattCharacteristicGroupData);
    	}

    	SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(this,
    			gattServiceData,
    			android.R.layout.simple_expandable_list_item_2,
    			new String[] {LIST_NAME, LIST_UUID},
    			new int[] {android.R.id.text1, android.R.id.text2},
    			gattCharacteristicData,
    			android.R.layout.simple_expandable_list_item_2,
    			new String[] {LIST_NAME, LIST_UUID},
    			new int[] {android.R.id.text1, android.R.id.text2}
    	);
    	mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
    	final IntentFilter intentFilter= new IntentFilter();
    	intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
    	intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
    	intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED);
    	intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
    	return intentFilter;
    }
}
