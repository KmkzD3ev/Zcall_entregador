package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.zenitech.zcallmobile.adapters.DadosContatosAdapter;
import br.com.zenitech.zcallmobile.domais.DadosContatos;
import br.com.zenitech.zcallmobile.interfaces.IDadosContatos;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Contatos extends AppCompatActivity {

    SharedPreferences prefs;
    Context context;
    RecyclerView rcContatos;
    LinearLayout llContatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;

        llContatos = findViewById(R.id.llContatos);
        rcContatos = findViewById(R.id.rcContatos);
        rcContatos.setLayoutManager(new LinearLayoutManager(context));

        findViewById(R.id.fabContatos).setOnClickListener(v -> finish());
        getContatos();
    }

    // VERIFICA SE EXISTE ENTREGAS
    private void getContatos() {
        if (new VerificarOnline().isOnline(context)) {
            try {
                //
                final IDadosContatos iContatos = IDadosContatos.retrofit.create(IDadosContatos.class);
                final Call<List<DadosContatos>> call = iContatos.contatos(
                        prefs.getString("id_empresa", ""),
                        "contatos",
                        prefs.getString("telefone", "")
                );
                call.enqueue(new Callback<List<DadosContatos>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<DadosContatos>> call, @NonNull Response<List<DadosContatos>> response) {
                        if (response.isSuccessful()) {
                            List<DadosContatos> dados = response.body();
                            runOnUiThread(() -> {
                                if (dados != null) {
                                    if (!dados.get(0).nome.equalsIgnoreCase("")) {
                                        Log.i("Contatos", dados.get(0).nome);
                                        Log.i("Contatos", dados.get(0).telefone);

                                        //
                                        DadosContatosAdapter adapter = new DadosContatosAdapter(context, dados);
                                        adapter.notifyDataSetChanged();
                                        rcContatos.setAdapter(adapter);

                                        //
                                        llContatos.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                            //myUpdateOperation();
                        } else {
                            //myUpdateOperation();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DadosContatos>> call, @NonNull Throwable t) {
                        //myUpdateOperation();
                    }
                });
            } catch (Exception ignored) {
                //myUpdateOperation();
            }
        } else {

            //myUpdateOperation();
        }
    }
}