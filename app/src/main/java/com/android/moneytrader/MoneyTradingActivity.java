package com.android.moneytrader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.moneytrader.AsyncTasks.SendMessageClient;
import com.android.moneytrader.AsyncTasks.SendMessageServer;
import com.android.moneytrader.Entities.Message;
import com.android.moneytrader.Receivers.WifiDirectBroadcastReceiver;
import com.android.moneytrader.util.ActivityUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MoneyTradingActivity extends Activity{
	private static final String TAG = "MoneyTradingActivity";
	private static final int REQUEST_PERMISSIONS_REQUIRED = 7;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiDirectBroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;
	private EditText edit;
	private static TextView moneyView;
	private static int myMoney;
	public static Handler handler;

	public static final int MESSAGE_READ = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_money_trading);
		moneyView = findViewById(R.id.moneyValue);
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mReceiver = WifiDirectBroadcastReceiver.createInstance();
		mReceiver.setmActivity(this, "trad");

		String[] PERMISSIONS = {
				Manifest.permission.CAMERA,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.RECORD_AUDIO
		};
		if (!hasPermissions(this, PERMISSIONS)) {
			ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_REQUIRED);
		}
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		startService(new Intent(this, MessageService.class));
		Button button = findViewById(R.id.sendMessage);
		edit = findViewById(R.id.editMessage);
		button.setOnClickListener(arg0 -> {

			//String msg = edit.getText().toString();

			//MainActivity.sendRecieve.write(msg.getBytes());

			if (!edit.getText().toString().equals("")) {
				sendMessage();
			} else {
				Toast.makeText(MoneyTradingActivity.this, "Please enter a not empty message", Toast.LENGTH_SHORT).show();
			}
		});
		if (getIntent() != null){
			String money = getIntent().getStringExtra("value");
			moneyView.setText(money);
			myMoney = Integer.parseInt(money);
		}

		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(@NonNull android.os.Message msg) {
				switch (msg.what){
					case MESSAGE_READ:
						byte[] readBuff = (byte[]) msg.obj;
						String tempMessage = new String(readBuff, 0, msg.arg1);
						moneyView.setText(tempMessage);
						break;
				}
				return true;
			}
		});

	}

	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		ActivityUtilities.customiseActionBar(this);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				Log.v(TAG, "Discovery process succeeded");
			}

			@Override
			public void onFailure(int reason) {
				Log.v(TAG, "Discovery process failed");
			}
		});
		saveStateForeground(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        saveStateForeground(false);
    }

	@Override
	public void onBackPressed() {
		AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
		newDialog.setTitle("Close Trading Room");
		newDialog.setMessage("Are you sure you want to close this Trading Room?\n"
				+ "You will no longer be able to receive money ");
		newDialog.setPositiveButton("Yes", (dialog, which) -> {
			if(MainActivity.server!=null){
				MainActivity.server.interrupt();
			}
			android.os.Process.killProcess(android.os.Process.myPid());
		});
		newDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
		newDialog.show();
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
		super.onStop();
	}

	public void sendMessage(){
		Message mes = new Message(edit.getText().toString(), null, MainActivity.userName);
		mes.setUser_record(MainActivity.loadUserName(this));
		if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER){
			new SendMessageServer(MoneyTradingActivity.this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mes);
		}
		else if(mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT){
			new SendMessageClient(MoneyTradingActivity.this, mReceiver.getOwnerAddr()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mes);
		}
		edit.setText("");
	}

	public static void updateMoney(Message message, boolean isMine){
		if(isMine){
			myMoney = myMoney - Integer.parseInt(message.getmText());
		}
		else{
			myMoney = myMoney + Integer.parseInt(message.getmText());
		}
		moneyView.setText(String.valueOf(myMoney));
    }

	public void saveStateForeground(boolean isForeground){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
  		Editor edit = prefs.edit();
  		edit.putBoolean("isForeground", isForeground);
  		edit.commit();
	}


}
