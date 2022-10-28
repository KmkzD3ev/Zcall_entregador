package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;

public class NovaEntrega extends AppCompatActivity {

    //
    private SharedPreferences prefs;
    private Context context;
    View mContentView;

    //
    String id_pedido = "";

    //
    private Vibrator rr;
    private MediaPlayer mp;
    long milliseconds = 1000;
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_entrega);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        //VIBRAR AO LER O CÓDIGO
        rr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //
        mp = MediaPlayer.create(NovaEntrega.this, R.raw.gas);
        mp.setVolume(1.0f, 1.0f);

        //VIBRAR APARELHO
        long[] vibration = new long[]{0, 400, 200, 400};
        rr.vibrate(vibration, -1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rr.vibrate(VibrationEffect.createOneShot(milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            rr.vibrate(milliseconds);
        }

        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
            mp.setLooping(true);
        }

        //TOCAR SOM DO GÁS
        mp.start();

        TextView txtInfor = findViewById(R.id.txtInfor);

        //
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_animation);
        txtInfor.startAnimation(animation);

        criarConexao();

        findViewById(R.id.btnAceitarEntrega).setOnClickListener(view -> {
            entregasRepositorio.entregaNotificada(id_pedido);
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

    private void criarConexao() {
        try {
            dataBaseOpenHelper = new DataBaseOpenHelper(context);
            conexao = dataBaseOpenHelper.getWritableDatabase();
            entregasRepositorio = new EntregasRepositorio(conexao);
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }
}
