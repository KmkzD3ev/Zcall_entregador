package br.com.zenitech.zcallmobile.Service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title;
        String body;

        // .getNotification está dando erro
        try {

            title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
            body = remoteMessage.getNotification().getBody();
        } catch (Exception e) {

            title = "Nova Entrega";//remoteMessage.getNotification().getTitle();
            body = "Toque para carregar os dados da nova entrega!";//remoteMessage.getNotification().getBody();
        }

        MyNotificationManager.getInstance(getApplicationContext()).displayNotification(title, body);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
    }
}
