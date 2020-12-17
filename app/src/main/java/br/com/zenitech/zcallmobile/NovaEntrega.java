package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovaEntrega extends AppCompatActivity {

    //
    private SharedPreferences prefs;
    private Context context;
    private SpotsDialog dialog;
    View mContentView;

    //
    String id_pedido = "";
    int t = 0;

    //
    private Vibrator rr;
    private MediaPlayer mp;
    long milliseconds = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_entrega);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Zcall Mobile");
        //getSupportActionBar().setSubtitle("Nova Entrega");

        //*******************************PP

        mContentView = findViewById(R.id.activity_nova_entrega);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        //dialog = new SpotsDialog(context, R.style.Custom);
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        //VIBRAR AO LER O CÓDIGO
        rr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        rr.vibrate(milliseconds);
//        rr.vibrate(milliseconds);

        //
        mp = MediaPlayer.create(NovaEntrega.this, R.raw.gas);
        mp.setVolume(1.0f, 1.0f);

        //mp.setLooping(true);
        //Bipe
        //mp.start();

        //beep();

        //
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {
                textos(params.getString("id_pedido"));
            }
        }

        findViewById(R.id.btnAceitarEntrega).setOnClickListener(view -> {

            /*VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                marcarComoVisto(id_pedido);
            } else {

                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }*/

            Intent i = new Intent(context, Principal2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.release();
        rr.cancel();
    }

    public void marcarComoVisto(String id_pedido) {
        //barra de progresso pontos
        //dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.marcarComoVisto(
                prefs.getString("id_empresa", ""),
                "marcarComoVisto",
                id_pedido);

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {

                if (response.isSuccessful()) {

                    //int code = response.code();
                    //if (code == 200)

                    DadosEntrega dados = response.body();

                    //
                    if (dados != null) {

                        if (dados.status.equals("OK")) {

                            //mp.release();
                            //
                            Intent i = new Intent(context, Principal2.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                } else {
                    Intent i = new Intent(context, Principal2.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }

                //
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<DadosEntrega> call, Throwable t) {

                //
                dialog.dismiss();

                //
                //Toast.makeText(context, "Problema de acesso: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }

    public void finalizarPedido(String id_pedido, String status) {
        //barra de progresso pontos
        dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.getAceitarEntrega(
                prefs.getString("id_empresa", ""),
                "aceitarentrega",
                id_pedido,
                status);

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {

                if (response.isSuccessful()) {

                    //int code = response.code();
                    //if (code == 200)

                    DadosEntrega dados = response.body();

                    //
                    if (dados != null) {

                        if (dados.status.equals("OK")) {

                            mp.release();
                            //
                            Intent i = new Intent(context, Principal2.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                } else {
                    // segura os erros de requisição
                    //ResponseBody errorBody = response.errorBody();
                }

                //
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<DadosEntrega> call, Throwable t) {

                //
                dialog.dismiss();

                //
                //Toast.makeText(context, "Problema de acesso: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void beep() {
        // ESPERA 4 SEGUNDOS PARA MOSTRAR O WEBVIEW
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //mp.setLooping(true);
                //
                rr.vibrate(milliseconds);
                //Bipe
                //mp.start();
                //mp.release();
                beep();

            }
        }, 700);
    }

    //
    private void textos(String totPedido) {
        TextView txtInfor = findViewById(R.id.txtInfor);
        Button btnAceitarEntrega = findViewById(R.id.btnAceitarEntrega);
        String info = "Você tem 1 nova entrega!";
        String btn = "VER ENTREGA";

        /*
        if (totPedido.equals("1")) {
            info = "Você tem 1 nova entrega!";
            btn = "VER ENTREGA";
        } else {
            info = "Você tem " + totPedido + " novas entregas!";
            btn = "VER ENTREGAS";
        }
        */
        txtInfor.setText(info);
        btnAceitarEntrega.setText(btn);

        //
        id_pedido = totPedido;

        //
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation);
        txtInfor.startAnimation(animation);

        //VIBRAR APARELHO
        long[] vibration = new long[]{0, 400, 200, 400};
        rr.vibrate(vibration, -1);
        //rr.vibrate(milliseconds);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rr.vibrate(VibrationEffect.createOneShot(500,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            rr.vibrate(500);
            //rr.vibrate(vibration, -1);
        }

        //TOCAR SOM DO GÁS
        mp.start();

        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
            mp.setLooping(true);
        }
    }
}
