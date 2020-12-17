package br.com.zenitech.zcallmobile.Service;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import br.com.zenitech.zcallmobile.R;

public class BatteryLevelReceiver extends BroadcastReceiver {

    private final boolean V_LOG = true;
    private final String TAG = "BatteryLevelReceiver";

    public static boolean StatusCarrenado = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // check to see what level we're being informed about
        if (V_LOG) {
            Log.v(TAG, "onReceive method called");
        }

        // check on the action associated with the intent
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            // notification that the battery is low
            if (V_LOG) {
                Log.v(TAG, "received notification that battery is low");
            }

            // inform the user
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
            mBuilder.setMessage(R.string.system_battery_status_low)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog mAlert = mBuilder.create();
            mAlert.show();

        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
            if (V_LOG) {
                Log.v(TAG, "received notification that battery is ok");
            }

            // inform the user
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
            mBuilder.setMessage(R.string.system_battery_status_ok)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog mAlert = mBuilder.create();
            mAlert.show();
        } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            if (V_LOG) {
                Log.v(TAG, "ACTION_POWER_CONNECTED");
            }

            StatusCarrenado = true;
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            if (V_LOG) {
                Log.v(TAG, "ACTION_POWER_DISCONNECTED");
            }

            StatusCarrenado = false;
        }
    }
}
