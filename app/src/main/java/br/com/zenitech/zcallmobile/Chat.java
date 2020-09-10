package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.adapters.MessageListAdapter;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends AppCompatActivity {

    RecyclerView mMessageRecycler;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //CARREGAR LISTA DE ENTREGAS
        getEntregas();
    }

    //
    private void getEntregas() {

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<List<DadosEntrega>> call = iEmpregos.getEntregas(
                prefs.getString("id_empresa", ""),
                "listarentregas",
                prefs.getString("telefone", ""));

        call.enqueue(new Callback<List<DadosEntrega>>() {
            @Override
            public void onResponse(Call<List<DadosEntrega>> call, Response<List<DadosEntrega>> response) {

                //
                if (response.isSuccessful()) {


                    //
                    final List<DadosEntrega> listarDados = response.body();

                    //
                    if (listarDados.get(0).id_pedido != null) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                MessageListAdapter mMessageAdapter = new MessageListAdapter(getBaseContext(), listarDados);
                                mMessageRecycler.setAdapter(mMessageAdapter);
                            }
                        });
                    } else {
                        //
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                //mRecyclerView.setVisibility(View.GONE);
                                //txtEntregas.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DadosEntrega>> call, Throwable t) {
                //
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
