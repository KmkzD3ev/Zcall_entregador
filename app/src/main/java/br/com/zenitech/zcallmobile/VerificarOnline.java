package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Kleilson Sousa on 12/12/17.
 */

class VerificarOnline {

    boolean isOnline(Context context) {
        boolean conectado = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            cm.getActiveNetworkInfo();
            // VERIFICA WIFI
            if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                conectado = true;
            }
            // VERIFICA O 2G,3G,4G
            else if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
                conectado = true;
            }
        }
        //NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        //return netInfo != null && netInfo.isConnected();

        return conectado;
    }
}
