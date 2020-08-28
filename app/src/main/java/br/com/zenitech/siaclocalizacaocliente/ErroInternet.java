package br.com.zenitech.siaclocalizacaocliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class ErroInternet extends AppCompatActivity {

    Button conexao, reiniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erro_internet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        conexao = (Button) findViewById(R.id.btnConexao);
        reiniciar = (Button) findViewById(R.id.btnReiniciar);

        conexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        });

        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ErroInternet.this, Splash.class));
                finish();
            }
        });
    }

}
