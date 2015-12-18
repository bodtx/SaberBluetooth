package org.bodtx.saber;

import java.lang.ref.WeakReference;

import org.bodtx.saber.MainActivity.BLeThread;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

public class BLEHandler extends Handler {
	private final WeakReference<MainActivity> mTarget;

	public BLEHandler(MainActivity context) {
		mTarget = new WeakReference<MainActivity>((MainActivity) context);
	}

	@Override
	public void handleMessage(android.os.Message msg) {
		mTarget.get().textView.setText(msg.getData().getString(
				MainActivity.statusMsg));


	};
}
