package br.com.zenitech.zcallmobile;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Objects;

import br.com.zenitech.zcallmobile.Service.Constants;

public class Splash extends AppCompatActivity {

    //
    private SharedPreferences prefs;
    private Context context;
    View mContentView;
    TextView versao;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;

        if (prefs.getBoolean("reset", false)) {
            //
            Intent i = new Intent(this, ResetApp.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Uri alarme = Uri.parse("android.resource://"
                    + context.getPackageName() + "/" + R.raw.gas);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();

            NotificationChannel mChannel =
                    new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setSound(alarme, audioAttributes);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);
        }

        //*******************************

        mContentView = findViewById(R.id.activity_splash);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //
        versao = findViewById(R.id.versao_s);
        versao.setText(String.format("Versão %s", BuildConfig.VERSION_NAME));

        //JobService
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ComponentName componentName = new ComponentName(this, JobServiceAux.class);

            PersistableBundle b = new PersistableBundle();
            b.putString("string", "Qualquer coisa");

            JobInfo info = new JobInfo.Builder(123, componentName)
                    .setBackoffCriteria(5000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setExtras(b)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)//JobInfo.NETWORK_TYPE_UNMETERED - Só para conexções wifi - Todos - NETWORK_TYPE_ANY
                    .setPersisted(true)
                    .setPeriodic(2000)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build();

            JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            js.schedule(info);

            /*
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");
            } else {
                Log.d(TAG, "Job scheduling failed");
            }
            */
        } else {

            //ALARME
            boolean alarmeAtivo = (PendingIntent.getBroadcast(this, 0, new Intent("ENTREGAS"), PendingIntent.FLAG_NO_CREATE) == null);

            String TAG = "SplashScreen";
            if (alarmeAtivo) {
                Log.i(TAG, "Sistema de consulta no background iniciado!");

                Intent intent = new Intent("ENTREGAS");
                PendingIntent p = PendingIntent.getBroadcast(this, 0, intent, 0);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.add(Calendar.SECOND, 3);

                AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarme != null) {
                    alarme.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 60000, p);
                }
            } else {
                Log.i(TAG, "Processo de consulta já foi iniciado!");
            }
        }

        // ESPERA 4 SEGUNDOS PARA MOSTRAR O WEBVIEW
        new Handler().postDelayed(() -> {

            //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
            if (Objects.requireNonNull(prefs.getString("telefone", "")).isEmpty()) {
                //
                Intent i = new Intent(context, Configuracao.class);
                i.putExtra("splash", "sim");
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

            } else if (Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {
                //
                Intent i = new Intent(context, Ponto.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

            }
            //SE EXISTIR USUÁRIO VAI PARA TELA PRINCIPAL
            else {
                //
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

            //
            fechar();

        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void fechar() {
        super.finish();
    }
}
