package com.android.moneytrader.AsyncTasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import com.android.moneytrader.MoneyTradingActivity;
import com.android.moneytrader.MainActivity;
import com.android.moneytrader.Entities.Message;
import com.android.moneytrader.InitThreads.ServerInit;

public class SendMessageServer extends AsyncTask<Message, Message, Message>{
	private static final String TAG = "SendMessageServer";
	private Context mContext;
	private static final int SERVER_PORT = 4446;
	private boolean isMine;

	public SendMessageServer(Context context, boolean mine){
		mContext = context;
		isMine = mine;
	}
	
	@Override
	protected Message doInBackground(Message... msg) {
		publishProgress(msg);
		try {
			ArrayList<InetAddress> listClients = ServerInit.clients;
			for(InetAddress addr : listClients){
				msg[0].setUser_record(MainActivity.loadUserName(mContext));
				if(msg[0].getSenderAddress()!=null && addr.getHostAddress().equals(msg[0].getSenderAddress().getHostAddress())){
					return msg[0];
				}
				Socket socket = new Socket();
				socket.setReuseAddress(true);
				socket.bind(null);
				socket.connect(new InetSocketAddress(addr, SERVER_PORT));
				OutputStream outputStream = socket.getOutputStream();
				new ObjectOutputStream(outputStream).writeObject(msg[0]);
			    socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg[0];
	}

	@Override
	protected void onProgressUpdate(Message... values) {
		super.onProgressUpdate(values);
		if(isActivityRunning(MainActivity.class)){
			MoneyTradingActivity.updateMoney(values[0], isMine);
		}
	}

	@Override
	protected void onPostExecute(Message result) {
		super.onPostExecute(result);
	}
	
	@SuppressWarnings("rawtypes")
	public Boolean isActivityRunning(Class activityClass)
	{
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }
        return false;
	}
}