package org.bodtx.saber;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public SoundPool soundPool;
	int saberonId;
	int saberoffId;
	int pulseId;
	boolean isOn;

	protected void beforeFinish() {

		try {
			socket.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		message += "Fermeture Flux\n";
		textView.setText(message);

		new CountDownTimer(5000, 1000) {
			Bundle messageBundle = new Bundle();

			public void onTick(long millisUntilFinished) {
				message += ("Fermeture dans: " + millisUntilFinished / 1000 + "\n");
				messageBundle.putString(statusMsg, message);
				Message messageAndroid = handler.obtainMessage();
				messageAndroid.setData(messageBundle);
				handler.sendMessage(messageAndroid);
			}

			public void onFinish() {
				finish();
			}
		}.start();
	}

	static final String TAG = "MyActivity";
	static final String statusMsg = "STATUSBLE";
	String message = "";
	BLEHandler handler = new BLEHandler(this);
	BLEBroadcastReceiver mReceiver = new BLEBroadcastReceiver(this);
	Bundle messageBundle = new Bundle();
	TextView textView;
	ImageView image;
	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothSocket socket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		saberonId = soundPool.load(this.getApplicationContext(), R.raw.saberon, 1);
		saberoffId= soundPool.load(this.getApplicationContext(), R.raw.saberof, 1);
		pulseId= soundPool.load(this.getApplicationContext(), R.raw.pulse, 1);
		
		setContentView(R.layout.activity_main);
		textView = (TextView) findViewById(R.id.edit_message);
		image = (ImageView) findViewById(R.id.imageView1);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			message += "bluetooth non supporté";
			textView.append(message);
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.enable();
			} else {
				new Thread(new BLeThread()).start();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(mReceiver);
	}

	class BLeThread implements Runnable {

		Message messageAndroid;

		public void sendMessage(String mess) {
			messageBundle.putString(statusMsg, message);
			messageAndroid = handler.obtainMessage();
			messageAndroid.setData(messageBundle);
			handler.sendMessage(messageAndroid);
		}

		@Override
		public void run() {

			socket = null;
			BluetoothDevice device = null;
			Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
			for (BluetoothDevice bondedDevice : bondedDevices) {
				if (bondedDevice.getName().equals("HC-05")) {
					device = bondedDevice;
				}
			}
			if (device == null) {
				message += "Erreur HC-05 non appairé\n";
				sendMessage(message);
			}
			// BluetoothDevice device = mBluetoothAdapter
			// .getRemoteDevice("20:13:10:15:38:91");
			message += "Accrochage distant\n";
			sendMessage(message);

			mBluetoothAdapter.cancelDiscovery();
			try {
				socket = device.createRfcommSocketToServiceRecord(UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				socket.connect();
				message += "Connexion\n";
				sendMessage(message);

				Thread.sleep(1000);
				// if (socket.getInputStream().available() > 0) {
				int playPulseId = 0;
				while (true) {
					int state = socket.getInputStream().read();
					if (49 == state) {
						if(isOn){
							soundPool.stop(playPulseId);
							soundPool.play(saberoffId, 1, 1, 1, 0, 1f);
							isOn = false;
							
						}else{
							sendMessage(message +=state);
							soundPool.play(saberonId, 1, 1, 1, 0, 1f);
							playPulseId = soundPool.play(pulseId, 1, 1, 1, -1, 1f);
							isOn = true;
						}
						
						
					} else {
						sendMessage(message += state);
					}

				}

			} catch (Exception e) {
				Log.e(TAG, e.toString());
				message += e.getMessage() + "\n";
				messageBundle.putInt("image", -1);
				sendMessage(message);
			} finally {

				messageBundle.putBoolean("done", true);
				sendMessage(message);
			}
		}

	}

}
