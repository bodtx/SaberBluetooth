package org.bodtx.saber;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class BLEBroadcastReceiver extends BroadcastReceiver {

		private MainActivity mainActivity;

		public BLEBroadcastReceiver(MainActivity mainActivity) {
			this.mainActivity = mainActivity;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (state) {
				case BluetoothAdapter.STATE_ON:
					mainActivity.message += "Bluetooth activé\n";
					mainActivity.textView.setText(mainActivity.message);
					new Thread(mainActivity.new BLeThread()).start();
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					mainActivity.message += "Activation Bluetooth ...\n";
					mainActivity.textView.setText(mainActivity.message);
					break;
				}
			}
		}
	};