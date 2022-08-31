package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MenuApp extends AppCompatActivity {
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        findViewById(R.id.btn_menu_entregador).setOnClickListener(v -> {
            Intent i = new Intent(this, Usuario.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_menu_contatos).setOnClickListener(v -> {
            Intent i = new Intent(this, Contatos.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_menu_reset).setOnClickListener(v -> {

            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.fabMenu).setOnClickListener(v -> finish());
    }
}