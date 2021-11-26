package com.android.moneytrader.Receivers;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.android.moneytrader.MainActivity;
import com.android.moneytrader.MoneyTradingActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/*
 * This class implements the Singleton pattern
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
	public static final int IS_OWNER = 1;
	public static final int IS_CLIENT = 2;
	private static final String TAG = "WifiDirectBR";

	private WifiP2pManager mManager;
	private Channel mChannel;
	private Activity mActivity;
	private List<String> peersName = new ArrayList<String>();
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private int isGroupeOwner;
	private InetAddress ownerAddr;
	private String activityCheck;
	private Boolean check = false;

	private static WifiDirectBroadcastReceiver instance;

	private WifiDirectBroadcastReceiver() {
		super();
	}

	public static WifiDirectBroadcastReceiver createInstance() {
		if (instance == null) {
			instance = new WifiDirectBroadcastReceiver();
		}
		return instance;
	}

	public int isGroupeOwner() {
		return isGroupeOwner;
	}

	public InetAddress getOwnerAddr() {
		return ownerAddr;
	}

	public void setmManager(WifiP2pManager mManager) {
		this.mManager = mManager;
	}

	public void setmChannel(Channel mChannel) {
		this.mChannel = mChannel;
	}

	public void setmActivity(Activity mActivity, String activityCheck) {
		if(activityCheck.equals("main")) {
			this.activityCheck = activityCheck;
			this.mActivity = (MainActivity) mActivity;
		}else if(activityCheck.equals("trad")){
			this.activityCheck = activityCheck;
			this.mActivity = (MoneyTradingActivity) mActivity;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
			Log.v(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				Toast.makeText(mActivity, "Wifi P2P is supported by this device", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mActivity, "Wifi P2P is not supported by this device", Toast.LENGTH_SHORT).show();
			}
		} else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
			Log.v(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");


			if(mManager == null){
				return;
			}

			if (mManager != null) {
				if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

					return;
				}

				if(activityCheck.equals("main")) {
					mManager.requestPeers(mChannel, ((MainActivity)mActivity).peerListListener);
				}else{

				}

			}


		} else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
			Log.v(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

		} else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
			Log.v(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");


			NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()){
				check = true;
				mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {

					@Override
					public void onConnectionInfoAvailable(WifiP2pInfo info) {
						ownerAddr= info.groupOwnerAddress;
						if (info.groupFormed && info.isGroupOwner) {
							isGroupeOwner = IS_OWNER;
							activateMoneyTradingButton("server");

						}
						else if (info.groupFormed) {
							isGroupeOwner = IS_CLIENT;
							activateMoneyTradingButton("client");

						}
					}
				});
			}
		}
	}

	public void activateMoneyTradingButton(String role){
		if(mActivity.getClass() == MainActivity.class) {
			((MainActivity) mActivity).getGoToMoneyTrading().setVisibility(View.VISIBLE);
			((MainActivity) mActivity).getSetUsername().setVisibility(View.VISIBLE);
			((MainActivity) mActivity).getSetMoney().setVisibility(View.VISIBLE);
			((MainActivity) mActivity).getSetUserNameLabel().setVisibility(View.VISIBLE);
			((MainActivity) mActivity).getDisconnect().setVisibility(View.VISIBLE);
			((MainActivity) mActivity).getGoToSettings().setVisibility(View.GONE);
			if (check){
				((MainActivity) mActivity).getLstView().setVisibility(View.GONE);
	     	}
			((MainActivity)mActivity).getGoToSettingsText().setVisibility(View.GONE);
		}
	}

}
