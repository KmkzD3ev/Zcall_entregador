package br.com.zenitech.zcallmobile.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.zenitech.zcallmobile.Splash;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, Splash.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
