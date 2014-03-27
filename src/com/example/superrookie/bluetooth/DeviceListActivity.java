package com.example.superrookie.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import com.example.superrookie.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class DeviceListActivity extends Activity {
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;
	
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private static final int REQUEST_ENABLE_BT = 1;
	
	private BluetoothAdapter mBluetoothAdapter;
	private DeviceListAdapter mPairedDevicesArrayAdapter;
	private DeviceListAdapter mNewDevicesArrayAdapter;
	private boolean mScanning;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Device Scan");		
		
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
						
			setResult(Activity.RESULT_CANCELED, intent);
			finish();
			return;
		}		
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);				
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:			
			mNewDevicesArrayAdapter.clear();
			doDiscovery();
			break;
		case R.id.menu_stop:
			stopDiscovery();
			break;
		}
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		
		mPairedDevicesArrayAdapter = new DeviceListAdapter();
		mNewDevicesArrayAdapter = new DeviceListAdapter();
		
		ListView pairedListView = (ListView)findViewById(R.id.paired_devices);		
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		ListView newDevicesListView = (ListView)findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();	
		
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);	
		} else {
			findViewById(R.id.title_paired_devices).setVisibility(View.GONE);
		}
		
		for (BluetoothDevice device : pairedDevices) {
			mPairedDevicesArrayAdapter.addDevice(device);
		}
		mPairedDevicesArrayAdapter.notifyDataSetInvalidated();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		
		this.unregisterReceiver(mReceiver);
	}
	
	
	private void doDiscovery() {
		if (D) Log.d(TAG, "doDiscovery()");
		
		mScanning = true;
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}		
		mBluetoothAdapter.startDiscovery();
		invalidateOptionsMenu();
	}
	
	private void stopDiscovery() {
		mScanning = false;
		mBluetoothAdapter.cancelDiscovery();
		invalidateOptionsMenu();
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int position, long id) {
			mBluetoothAdapter.cancelDiscovery();			
			DeviceListAdapter adapter = (DeviceListAdapter)av.getAdapter();
			BluetoothDevice device = adapter.getDevice(position);			
			String address = device.getAddress();
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
						
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};	
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.addDevice(device);
					mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {				
				stopDiscovery();			
			}
		}
	};
	
	private class DeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mDevices;
		private LayoutInflater mInflator;
		
		public DeviceListAdapter() {
			super();
			mDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceListActivity.this.getLayoutInflater();
		}
		
		public void addDevice(BluetoothDevice device) {
			if (!mDevices.contains(device)) {
				mDevices.add(device);
			}
		}
		
		public BluetoothDevice getDevice(int position) {
			return mDevices.get(position);
		}
		
		public void clear() {
			mDevices.clear();
		}
		
		@Override
		public int getCount() {
			return mDevices.size();
		}
		
		@Override
		public Object getItem(int i) {
			return mDevices.get(i);
		}
		
		@Override
		public long getItemId(int i) {
			return i;
		}
		
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView)view.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView)view.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)view.getTag();
			}
			
			BluetoothDevice device = mDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0) {
				viewHolder.deviceName.setText(deviceName);
			} else {
				viewHolder.deviceName.setText(R.string.unknown_device);
			}
			viewHolder.deviceAddress.setText(device.getAddress());
			
			return view;
		}
	}
	
	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}
