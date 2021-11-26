package com.android.moneytrader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.moneytrader.InitThreads.ClientInit;
import com.android.moneytrader.InitThreads.SendRecieve;
import com.android.moneytrader.InitThreads.ServerInit;
import com.android.moneytrader.Receivers.WifiDirectBroadcastReceiver;
import com.android.moneytrader.util.ActivityUtilities;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	public static final String DEFAULT_USER_NAME = "";

	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiDirectBroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;
	private Button gotoMoneyTrading;
	private ImageView goToSettings;
	private TextView goToSettingsText;
	private TextView setUserNameLabel;
	private EditText setUsername;
	private LinearLayout settingLayout;
	private ListView listView;
	private EditText setMoney;
	private ImageView disconnect;
	public static String userName;
	public static ServerInit server;
	//public static SendRecieve sendRecieve;
	private WifiManager wifiManager;

	private WifiP2pDevice[] deviceArray;
	private String[] deviceNameArray;
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	public Button getGoToMoneyTrading() {
		return gotoMoneyTrading;
	}

	public TextView getSetUserNameLabel() {
		return setUserNameLabel;
	}

	public ImageView getGoToSettings() {
		return goToSettings;
	}

	public ListView getLstView(){
		return listView;
	}

	public EditText getSetUsername() {
		return setUsername;
	}

	public EditText getSetMoney() {
		return setMoney;
	}

	public TextView getGoToSettingsText() {
		return goToSettingsText;
	}

	public ImageView getDisconnect() {
		return disconnect;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();

		checkRunTimePermission();

		goToSettingsText = findViewById(R.id.textGoToSettings);
		setMoney = findViewById(R.id.setMoney);
		gotoMoneyTrading = findViewById(R.id.moneyTradingButton);
		goToMoneyTrading();
		setUsername = findViewById(R.id.setUsername);
		setUserNameLabel = findViewById(R.id.setUserNameLabel);
		listView = findViewById(R.id.peerListView);
		goToSettings = findViewById(R.id.goToSettings);
		goToSettings();
		settingLayout = findViewById(R.id.settingLayout);
		setUsername.setText(loadUserName(this));
		disconnect = findViewById(R.id.moneyImage);
		disconnect();
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
				listView.setVisibility(View.GONE);
				settingLayout.setVisibility(View.VISIBLE);
			}
		});

	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	public void init() {
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mReceiver = WifiDirectBroadcastReceiver.createInstance();
		mReceiver.setmManager(mManager);
		mReceiver.setmChannel(mChannel);
		mReceiver.setmActivity(this, "main");

		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}

	public void goToMoneyTrading() {
		gotoMoneyTrading.setOnClickListener(arg0 -> {
			String money = setMoney.getText().toString();
			if (money.isEmpty()) {
				Toast.makeText(this, "Enter Amount for money", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!setUsername.getText().toString().equals("")) {
				saveUserName(MainActivity.this, setUsername.getText().toString());
				userName = loadUserName(MainActivity.this);
				if (mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER) {
					Toast.makeText(MainActivity.this, "I'm the group owner  " + mReceiver.getOwnerAddr().getHostAddress(), Toast.LENGTH_SHORT).show();

					server = new ServerInit();
					server.start();
				} else if (mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT) {
					Toast.makeText(MainActivity.this, "I'm the client", Toast.LENGTH_SHORT).show();
					ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
					client.start();
				}
				Intent intent = new Intent(getApplicationContext(), MoneyTradingActivity.class);
				intent.putExtra("value", money);
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "Please enter a user name", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void disconnect() {
		disconnect.setOnClickListener(v -> {
			mManager.removeGroup(mChannel, null);
			finish();
		});
	}


	public void goToSettings() {

		goToSettings.setOnClickListener(arg0 -> {
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}

			checkLocationService();


			registerReceiver(mReceiver, mIntentFilter);

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

				return;
			}
			mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
				@Override
				public void onSuccess() {
					Log.v(TAG, "Discovery process succeeded");

				}

				@Override
				public void onFailure(int reason) {
					Log.v(TAG, "Discovery process failed");
					listView.setVisibility(View.GONE);
					settingLayout.setVisibility(View.VISIBLE);
				}
			});

			//startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
		});


		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final WifiP2pDevice device = deviceArray[position];
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;

				if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Toast.makeText(getApplicationContext(), "Connected to "+ device.deviceName, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int reason) {
						Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
    }
    
  	public void saveUserName(Context context, String userName) {
  		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
  		Editor edit = prefs.edit();
  		edit.putString("userName", userName);
  		edit.commit();
  	}

  	public static String loadUserName(Context context) {
  		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
  		return prefs.getString("userName", DEFAULT_USER_NAME);
  	}

	public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peerList) {

			if(!peerList.getDeviceList().equals(peers)){

				peers.clear();
				peers.addAll(peerList.getDeviceList());

				deviceNameArray = new String[peerList.getDeviceList().size()];
				deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

				int index = 0;

				for(WifiP2pDevice device: peerList.getDeviceList()){
					deviceNameArray[index] = device.deviceName;
					deviceArray[index] = device;
					index++;
				}

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
				listView.setAdapter(adapter);

			}

			if(peers.size()==0){
				Toast.makeText(getApplicationContext(), "No device found!", Toast.LENGTH_SHORT).show();
				listView.setVisibility(View.GONE);
				settingLayout.setVisibility(View.VISIBLE);
				return;
			}else{
				listView.setVisibility(View.VISIBLE);
				settingLayout.setVisibility(View.GONE);
			}
		}
	};

	public void checkRunTimePermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


			} else {
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						10);
			}
		} else {

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 10) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

			} else {
				checkLocationService();
				//code for deny
			}
		}
	}

	private void checkLocationService(){

		if(!isLocationEnabled(getApplicationContext())){
			if (!ActivityCompat.shouldShowRequestPermissionRationale((this) , Manifest.permission.ACCESS_FINE_LOCATION)) {
				// If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle("Permission Required");
				dialog.setCancelable(false);
				dialog.setMessage("You have to Allow permission to access user location");
				dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});
				AlertDialog alertDialog = dialog.create();
				alertDialog.show();
			}
		}


	}

	private boolean isLocationEnabled(Context mContext) {
		LocationManager locationManager = (LocationManager)
				mContext.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}


}