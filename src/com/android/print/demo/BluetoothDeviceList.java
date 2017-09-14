package com.android.print.demo;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class BluetoothDeviceList extends Activity {
    private static final String TAG = "DeviceListActivity";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_RE_PAIR = "re_pair";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ListView pairedListView;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setTitle(R.string.select_device);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        initView();
    }

    private void initView(){
    	// Initialize the button to perform device discovery
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setEnabled(false);
            }
        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);

        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setOnItemLongClickListener(mDeviceLongClickListener);
        pairedListView.setOnCreateContextMenuListener(mCreateContextMenuListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	mPairedDevicesArrayAdapter.add(device.getName()
            			+ " ( " + getResources().getText(R.string.has_paired) +" )"
            			+ "\n" + device.getAddress());
            }
        }
    }

    @Override
	protected void onStop() {
		// Make sure we're not doing discovery anymore
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
		// Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        super.onStop();
	}

	@Override
	protected void onResume() {
		// Register for broadcasts when a device is discovered and discovery has finished
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        super.onResume();
	}

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mPairedDevicesArrayAdapter.clear();
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private void returnToPreviousActivity(String address, boolean re_pair)
    {
    	// Cancel discovery because it's costly and we're about to connect
    	if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_RE_PAIR, re_pair);

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	// Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
        	returnToPreviousActivity(address, false);
        }
    };

    private OnItemLongClickListener mDeviceLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			//if return true, don't call method onCreateContextMenu
			return false;
		}
	};

	private OnCreateContextMenuListener mCreateContextMenuListener = new OnCreateContextMenuListener(){
		@Override
		public void onCreateContextMenu(ContextMenu menu, View view,ContextMenuInfo arg2) {
			menu.setHeaderTitle(R.string.select_options);

			String info = ((TextView)(((AdapterContextMenuInfo)arg2).targetView)).getText().toString();
			//if(((AdapterContextMenuInfo)arg2).position < pairedDeviceNum)
			if (info.contains(" ( " + getResources().getText(R.string.has_paired) +" )")) {
				menu.add(0, 0, 0, R.string.rePaire_connect).setOnMenuItemClickListener(mOnMenuItemClickListener);
				menu.add(0, 1, 1, R.string.connect_paired).setOnMenuItemClickListener(mOnMenuItemClickListener);
			}
			else
			{
				menu.add(0, 2, 2, R.string.paire_connect).setOnMenuItemClickListener(mOnMenuItemClickListener);
			}
		}
    };

	private final OnMenuItemClickListener mOnMenuItemClickListener = new OnMenuItemClickListener(){
		public boolean onMenuItemClick(MenuItem item) {
			String info = ((TextView)((AdapterContextMenuInfo)item.getMenuInfo()).targetView).getText().toString();
			String address = info.substring(info.length() - 17);
			switch (item.getItemId()) {
			case 0://repair and connect
				returnToPreviousActivity(address, true);
				break;
			case 1://connect
			case 2://pair and connect
				returnToPreviousActivity(address, false);
				break;
			}
			return false;
		}
	};

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                String itemName = device.getName()
            			+ " ( " + getResources().getText(device.getBondState() == BluetoothDevice.BOND_BONDED ? R.string.has_paired : R.string.not_paired) +" )"
            			+ "\n" + device.getAddress();

                mPairedDevicesArrayAdapter.remove(itemName);
            	mPairedDevicesArrayAdapter.add(itemName);
            	pairedListView.setEnabled(true);
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mPairedDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mPairedDevicesArrayAdapter.add(noDevices);
                    pairedListView.setEnabled(false);
                }
                scanButton.setEnabled(true);
            }
        }
    };

}
