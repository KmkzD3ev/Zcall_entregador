package br.com.zenitech.zcallmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ErroInternet extends AppCompatActivity {

    Button conexao, reiniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erro_internet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        conexao = (Button) findViewById(R.id.btnConexao);
        reiniciar = (Button) findViewById(R.id.btnReiniciar);

        conexao.setOnClickListener(view -> startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0));

        reiniciar.setOnClickListener(view -> {
            startActivity(new Intent(ErroInternet.this, Splash.class));
            finish();
        });
    }

}
