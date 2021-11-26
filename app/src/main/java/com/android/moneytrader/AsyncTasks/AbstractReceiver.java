package com.android.moneytrader.AsyncTasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.android.moneytrader.MainActivity;
import com.android.moneytrader.R;
import com.android.moneytrader.Entities.Message;

public class AbstractReceiver extends AsyncTask<Void, Message, Void>{
	
	@Override
	protected Void doInBackground(Void... params) {
		return null;
	}
	
	protected void playNotification(Context context, Message message){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		Notification mNotification = new Notification.Builder(context)
			.setContentTitle(message.getUserName())
			.setContentText(message.getmText())
			.setSmallIcon(R.drawable.ic_baseline__money_24)
			.setContentIntent(pIntent)
			.setSound(notification)			
			.build();
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.defaults |= Notification.DEFAULT_VIBRATE;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		if(!pref.getBoolean("isForeground", false)){
			mNotificationManager.notify(0, mNotification);
		}
	}
}
