package br.com.zenitech.siaclocalizacaocliente;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

import br.com.zenitech.siaclocalizacaocliente.domais.DadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.interfaces.IDadosEntrega;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Usuario extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private Context context;
    private SpotsDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Zcall Mobile");
        getSupportActionBar().setSubtitle("Entregador");

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        //dialog = new SpotsDialog(context, R.style.Custom);
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        findViewById(R.id.btnSairPonto).setOnClickListener(view -> {

            //
            VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                sairPonto();
            }
        });
    }

    public void sairPonto() {
        //barra de progresso pontos
        dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.getSairPonto(
                prefs.getString("id_empresa", ""),
                "sairponto",
                prefs.getString("telefone", ""));

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {


                int code = response.code();

                if (code == 200) {

                    DadosEntrega dados = response.body();

                    if (dados != null && dados.status.equals("OK")) {

                        prefs.edit().putString("ponto", "").apply();

                        //
                        Toast.makeText(Usuario.this, "O App foi finalizado!",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    }

                } else {
                    Toast.makeText(Usuario.this, "Falha" + String.valueOf(code),
                            Toast.LENGTH_SHORT).show();
                }

                //
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<DadosEntrega> call, Throwable t) {

                //
                dialog.dismiss();

                //
                Toast.makeText(context, "Problema de acesso: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //
                Sair();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //
        Sair();
    }

    private void Sair() {

        //
        Intent i = new Intent(context, Principal2.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}
