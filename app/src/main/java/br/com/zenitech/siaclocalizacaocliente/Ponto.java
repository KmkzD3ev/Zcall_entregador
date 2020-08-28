package br.com.zenitech.siaclocalizacaocliente;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

import br.com.zenitech.siaclocalizacaocliente.domais.DadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.interfaces.IDadosEntrega;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Ponto extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private Context context;
    private SpotsDialog dialog;
    private String newToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ponto);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Zcall Mobile");
        getSupportActionBar().setSubtitle("Ponto");

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        //dialog = new SpotsDialog(context, R.style.Custom);
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        //Gerar token firebase **********
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Ponto.this, instanceIdResult -> {
            newToken = instanceIdResult.getToken();
            Log.e("Ponto", newToken);

            //prefs.edit().putString("fcmToken", newToken).apply();
        });

        findViewById(R.id.btnIniciarPonto).setOnClickListener(view -> {

            //
            VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                iniciarPonto();
            }
        });
    }

    public void iniciarPonto() {

        //barra de progresso pontos
        dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.getPonto(
                prefs.getString("id_empresa", ""),
                "iniciarponto",
                prefs.getString("telefone", ""),
                newToken
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {


                int code = response.code();

                if (code == 200) {

                    DadosEntrega dados = response.body();

                    if (Objects.requireNonNull(dados).status.equals("OK")) {

                        prefs.edit().putString("ponto", "ok").apply();
                        prefs.edit().putString("ativar_btn_ligar", dados.ativar_btn_ligar).apply();

                        //
                        Intent i = new Intent(Ponto.this, Principal2.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }

                } else {
                    Toast.makeText(Ponto.this, "Falha" + code,
                            Toast.LENGTH_SHORT).show();
                }

                //
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                //
                dialog.dismiss();

                //
                Toast.makeText(context, "Problema de acesso: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();

                Log.e("Ponto", t.getMessage());
            }
        });
    }
}
