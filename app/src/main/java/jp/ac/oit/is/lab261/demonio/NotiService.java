package jp.ac.oit.is.lab261.demonio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotiService extends Service {
    NotificationManager mNM;
    @Override
    public void onCreate()
    {
        Log.d("NotiService", "onCreate()");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NotiService", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("NotiService", "onDestroy");
        super.onDestroy();
        startService(new Intent(this, NotiService.class));
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    private void showNotification() {
        Log.d("NotiService", "showNotification");
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification notif= new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("タップしてアプリを開く")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .build();

        notif.flags = Notification.FLAG_ONGOING_EVENT;
        mNM.notify(1, notif);
    }
}