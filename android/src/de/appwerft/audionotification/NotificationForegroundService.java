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
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationForegroundService extends Service {
	private static final String PACKAGE_NAME = TiApplication.getInstance().getPackageName();
	static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
	private static final String LCAT = TiaudionotificationModule.LCAT;
	
	public static final String EXTRA_ACTION = "MYACTION";

	private final Context ctx;
	private final String packageName;
	private final String className;
	private boolean changingConfiguration;

	private NotificationManager notificationManager;
	private KrollDict notificationOpts;
	
	public NotificationForegroundService() {
		super();
		Log.d(LCAT,"NotificationForegroundService CONSTRUCTOR");
		ctx = TiApplication.getInstance().getApplicationContext();
		packageName = TiApplication.getInstance().getPackageName();
		className = packageName + "." + TiApplication.getAppRootOrCurrentActivity().getLocalClassName();
		Log.d(LCAT,"className="+className);
			
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LCAT, "getSystemService inside onCreate");
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	@Override
	public IBinder onBind(Intent intent) {
		// Called when a client comes to
		// the foreground
		// and binds with this service. The service should cease to be a
		// foreground service
		// when that happens.
		stopForeground(true);
		changingConfiguration = false;
		Log.d(LCAT,"onBind");
		return binder;//messenger.getBinder();
	}
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	private final IBinder binder = new LocalBinder();
	public class LocalBinder extends Binder {
		NotificationForegroundService getService() {
			Log.d(LCAT,"LocalBinder");
			return NotificationForegroundService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		changingConfiguration = true;
	}

	

	@Override
	public void onRebind(Intent intent) {
		// Called when a client (MainActivity in case of this sample) returns to
		// the foreground
		// and binds once again with this service. The service should cease to
		// be a foreground
		// service when that happens.
		Log.i(LCAT, "< ~~~~~ in onRebind()");
		stopForeground(true);
		changingConfiguration = false;
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(LCAT, "Last client unbound from service mChangingConfiguration=" + changingConfiguration);

		// Called when the last client (MainActivity in case of this sample)
		// unbinds from this
		// service. If this method is called due to a configuration change in
		// MainActivity, we
		// do nothing. Otherwise, we make this service a foreground service.
		if (!changingConfiguration) {
		getNotification();
			
		} else
			Log.w(LCAT, "onUnbind: was only a confchanging");
		// EventBus.getDefault().unregister(this);
		return true; // Ensures onRebind() is called when a client re-binds.
	}

	public void updateNotification(KrollDict opts) {
		notificationOpts = opts;
		if (opts.containsKeyAndNotNull(TiC.PROPERTY_TITLE)) {

		}
		
		getNotification();

	}

	public void hideNotification() {

	}

	// https://willowtreeapps.com/ideas/mobile-notifications-part-2-some-useful-android-notifications
	private void getNotification() {
		Log.d(LCAT, "getNotification!");
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.setComponent(new ComponentName(packageName, className));
		Log.d(LCAT, "::" + className);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
		// builder.setSmallIcon(R("applogo", "drawable"));
		// builder.setSmallIcon();
		builder.setContentTitle(notificationOpts.containsKeyAndNotNull(TiC.PROPERTY_TITLE)
				? notificationOpts.getString(TiC.PROPERTY_TITLE)
				: "TEST");
		builder.setContentText(notificationOpts.containsKeyAndNotNull(TiC.PROPERTY_SUBTITLE)
				? notificationOpts.getString(TiC.PROPERTY_SUBTITLE)
				: "UNTERTEST");
		builder.setLargeIcon(notificationOpts.containsKeyAndNotNull(Constants.LOGO.LOCAL)
				? (Bitmap) notificationOpts.get(Constants.LOGO.LOCAL)
				: null);

		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION.CHANNELID,
					Constants.NOTIFICATION.CHANNELNAME, NotificationManager.IMPORTANCE_DEFAULT);
			// channel.setDescription(NOTIFICATION_CHANNEL_DESC);
			NotificationManager notificationManager = (NotificationManager) getSystemService(
					Context.NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(channel);
		}
		Log.d(LCAT, "Notification created");
		Log.d(LCAT, notification.toString());
		
		notificationManager.notify(Constants.NOTIFICATION.ID, notification);
	}

	/**
	 * Handler of incoming messages from clients.
	 */
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

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger messenger = new Messenger(new IncomingHandler());

	/* helper function for safety getting resources */
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