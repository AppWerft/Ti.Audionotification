package de.appwerft.audionotification;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.*;
import android.util.Log;

public class NotificationForegroundService extends Service {
	private static final String PACKAGE_NAME = TiApplication.getInstance().getPackageName();
	static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
	private static final String LCAT = TiaudionotificationModule.LCAT + "_Service";
	public static final String EXTRA_ACTION = "MYACTION";
	private final Context ctx;
	private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

	private boolean changingConfiguration;
	private NotificationManager notificationManager;
	private KrollDict notificationOpts = new KrollDict();

	public NotificationForegroundService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		notificationOpts.put("title", "Title");
		notificationOpts.put("subtitle", "SubTitle");

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LCAT, "getSystemService inside onCreate");
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Android O requires a Notification Channel.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Create the channel for the notification
			NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION.CHANNELID,
					TiApplication.getInstance().getPackageName(), NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription("Channel description");
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
			channel.setVibrationPattern(new long[] { 0, 1000, 500, 1000 });
			channel.enableVibration(true);
			// Set the Notification Channel for the Notification Manager.
			notificationManager.createNotificationChannel(channel);
			Log.d(LCAT, "NotificationChannel added to NotificationManager");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LCAT, "onBind");
		stopForeground(true);
		changingConfiguration = false;
		return binder;// messenger.getBinder();
	}

	private final IBinder binder = new LocalBinder();

	public class LocalBinder extends Binder {

		NotificationForegroundService getService() {
			Log.d(LCAT, "NotificationForegroundService");
			return NotificationForegroundService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(LCAT, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(LCAT, "onConfigurationChanged");
		changingConfiguration = true;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.e(LCAT, "onRebind");
		stopForeground(true);
		changingConfiguration = false;
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (!changingConfiguration) {
			Notification notification = getNotification();
			Log.d(LCAT, (String) notification.toString());
			startForeground(Constants.NOTIFICATION.ID, notification);
			Log.d(LCAT, "notification started in Foreground");
		} else
			Log.w(LCAT, "onUnbind: was only a confchanging");
		return true; // Ensures onRebind() is called when a client re-binds.
	}

	public void updateNotification(KrollDict opts) {
		notificationOpts = opts;
		if (opts.containsKeyAndNotNull(TiC.PROPERTY_TITLE)) {
		}

	}

	public void hideNotification() {
	}

	// https://willowtreeapps.com/ideas/mobile-notifications-part-2-some-useful-android-notifications
	/**
	 * Returns the {@link NotificationCompat} used as part of the foreground
	 * service.
	 */
	private Notification getNotification() {

		Intent intent = new Intent(this, NotificationForegroundService.class);
		// Extra to help us figure out if we arrived in onStartCommand via the
		// notification or not.
		intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

		// The activityIntent calls the app
		Intent activityIntent = new Intent(Intent.ACTION_MAIN);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

		final String packageName = TiApplication.getInstance().getPackageName();
		activityIntent.setComponent(new ComponentName(packageName,
				packageName + "." + TiApplication.getAppRootOrCurrentActivity().getLocalClassName()));
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, activityIntent, 0);
		Log.d(LCAT, "intents ready, try build NotificationCompat.Builder\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");		
		// Building notification:
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(
				ctx/*, Constants.NOTIFICATION.CHANNELID
					 */);
		builder.setSmallIcon(R("applogo", "drawable"));
		builder.setDefaults(Notification.DEFAULT_ALL).setPriority(Notification.PRIORITY_HIGH)
				.setWhen(System.currentTimeMillis()).setOngoing(true);
		builder.setContentTitle(notificationOpts.containsKeyAndNotNull(TiC.PROPERTY_TITLE)
				? notificationOpts.getString(TiC.PROPERTY_TITLE)
				: "TEST");
		builder.setContentText(notificationOpts.containsKeyAndNotNull(TiC.PROPERTY_SUBTITLE)
				? notificationOpts.getString(TiC.PROPERTY_SUBTITLE)
				: "Ausführliche Botschaft…");
		/*
		 * Log.d(LCAT, "largeIcon");
		 * builder.setLargeIcon(notificationOpts.containsKeyAndNotNull(Constants.LOGO.
		 * LOCAL) ? (Bitmap) notificationOpts.get(Constants.LOGO.LOCAL) : null);
		 */
		Log.d(LCAT, pendingIntent.toString());
		builder.setContentIntent(pendingIntent);
		// Set the Channel ID for Android O.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// buildersetChannel(Constants.NOTIFICATION.CHANNELID);
			Log.d(LCAT, "setChannelId to " + Constants.NOTIFICATION.CHANNELID);
			builder.setChannelId(Constants.NOTIFICATION.CHANNELID); // Channel ID
		}
		Notification notification = builder.build();
		return notification;
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.NOTIFICATION.FOREGROUND_SERVICE:
				KrollDict opts = (KrollDict) msg.obj;
				updateNotification(opts);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	final Messenger messenger = new Messenger(new IncomingHandler());

	private int R(String name, String type) {
		int id = 0;
		try {
			id = this.getResources().getIdentifier(name, type, this.getPackageName());
		} catch (Exception e) {
			return id;
		}
		return id;
	}
}