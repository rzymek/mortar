package org.mortar.client;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ListenerService extends Service {
	private static final int NOTIFIFACTION_ID = 1337;
	public static final int CMD_EXIT = 1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final ListenerService thiz = this;
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.putExtra("CMD", CMD_EXIT);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0/* flags */);
		Notification notification = new NotificationCompat.Builder(this)
			.setContentIntent(resultPendingIntent)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("Mortar Client")
			.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(NOTIFIFACTION_ID, notification);
		
//		Timer timer = new Timer();
//		timer.schedule(new TimerTask() {			
//			@Override
//			public void run() {
//				Intent i = new Intent(thiz, InfoActivity.class);
//				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(i);
//			}
//		}, 5*1000);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}
	@Override
	public IBinder onBind(Intent intent) {
		return (null);
	}

}
