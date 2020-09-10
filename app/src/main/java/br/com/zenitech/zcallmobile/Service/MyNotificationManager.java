package br.com.zenitech.zcallmobile.Service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import br.com.zenitech.zcallmobile.R;
import br.com.zenitech.zcallmobile.Splash;

class MyNotificationManager {

    private Context contexto;
    @SuppressLint("StaticFieldLeak")
    private static MyNotificationManager mInstance;

    private MyNotificationManager(Context context) {
        contexto = context;
    }

    static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }

        return mInstance;
    }

    void displayNotification(String title, String body) {
        Uri alarme = Uri.parse("android.resource://"
                + contexto.getPackageName() + "/" + R.raw.gas);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(contexto, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_zcall_mobile)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(alarme)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        Intent intent = new Intent(contexto, Splash.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(contexto, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) contexto.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
