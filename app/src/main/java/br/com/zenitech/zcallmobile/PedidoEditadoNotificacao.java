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
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import dmax.dialog.SpotsDialog;

public class PedidoEditadoNotificacao extends AppCompatActivity {

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
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_editado_notificacao);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //*******************************PP

        mContentView = findViewById(R.id.activity_pedido_editado);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        criarConexao();
        //dialog = new SpotsDialog(context, R.style.Custom);
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        //VIBRAR AO LER O CÓDIGO
        rr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //
        mp = MediaPlayer.create(context, R.raw.notificacao2);
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
                id_pedido = params.getString("id_pedido");
            }
        }

        findViewById(R.id.btnAceitarEntrega).setOnClickListener(view -> {

           /* VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                marcarComoVisto(id_pedido);
            } else {

                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
*/
            entregasRepositorio.entregaNotificada(id_pedido);

            Intent i = new Intent(context, Principal2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            /*Intent in = new Intent(context, FinalizarEntrega.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("id_pedido", dadosEntrega.id_pedido);
            in.putExtra("nome_atendente", dadosEntrega.nome_atendente);
            in.putExtra("troco_para", dadosEntrega.troco_para);
            in.putExtra("valor", dadosEntrega.valor);
            in.putExtra("telefone_pedido", dadosEntrega.telefone_pedido);
            in.putExtra("id_cliente", dadosEntrega.id_cliente);
            in.putExtra("cliente", dadosEntrega.cliente);
            in.putExtra("coordCliLat", dadosEntrega.coord_latitude);
            in.putExtra("coordCliLon", dadosEntrega.coord_longitude);
            in.putExtra("apelido", dadosEntrega.apelido);
            in.putExtra("endereco", dadosEntrega.endereco);
            in.putExtra("numero", dadosEntrega.numero);
            in.putExtra("complemento", dadosEntrega.complemento);
            in.putExtra("ponto_referencia", dadosEntrega.ponto_referencia);
            in.putExtra("localidade", dadosEntrega.localidade);
            in.putExtra("produtos", dadosEntrega.produtos);
            in.putExtra("brindes", dadosEntrega.brindes);
            in.putExtra("observacao", dadosEntrega.observacao);
            in.putExtra("forma_pagamento", dadosEntrega.forma_pagamento);
            context.startActivity(in);*/

            finish();
        });

        /*if(ConfigApp.vrsaoPOS) {
            entregaNotificada(id_pedido);
        }*/
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

    //
    private void textos(String totPedido) {
        TextView txtInfor = findViewById(R.id.txtInfor);
        Button btnAceitarEntrega = findViewById(R.id.btnAceitarEntrega);
        String info = "O pedido: " + totPedido + " foi alterado!";
        String btn = "VER ALTERAÇÕES";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rr.vibrate(VibrationEffect.createOneShot(500,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            rr.vibrate(500);
        }

        //TOCAR SOM DO GÁS
        mp.start();
        mp.setLooping(true);
        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
            mp.setLooping(true);
        }
    }

    //
    private void criarConexao() {
        try {
            //
            dataBaseOpenHelper = new DataBaseOpenHelper(context);
            //
            conexao = dataBaseOpenHelper.getWritableDatabase();
            //
            entregasRepositorio = new EntregasRepositorio(conexao);
            //Toast.makeText(context, "Conexão criada com sucesso!", Toast.LENGTH_LONG).show();
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }
}