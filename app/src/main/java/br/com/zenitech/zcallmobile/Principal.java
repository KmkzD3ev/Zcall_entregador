package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.adapters.DadosEntregaAdapter;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Principal extends AppCompatActivity {
    //
    SharedPreferences prefs;
    private Context context;

    private SwipeRefreshLayout mySwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    TextView txtEntregas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setLogo(R.drawable.logobranca);

        //
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;

        if (Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {

            //
            startActivity(new Intent(Principal.this, Ponto.class));
            finish();
        }

        txtEntregas = findViewById(R.id.txtEntregas);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(Principal.this));

        //
        mySwipeRefreshLayout = findViewById(R.id.swiperefreshMainActivity);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mySwipeRefreshLayout.setRefreshing(true);

        // ANIMAÇÃO PARA CARREGAR PÁGINA
        /*
        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<List<DadosEntrega>> call = iEmpregos.getEntregas(prefs.getString("telefone", ""));

        call.enqueue(new Callback<List<DadosEntrega>>() {
            @Override
            public void onResponse(Call<List<DadosEntrega>> call, Response<List<DadosEntrega>> response) {
                myUpdateOperation();

                List<DadosEntrega> lista = response.body();

                int i = 0;

                //
                final List<DadosEntrega> listarDados = response.body();
                if (listarDados != null) {

                    for (DadosEntrega dadosEntrega : lista) {
                        if (dadosEntrega.getStatus().equals("")) {
                            i = 1;
                        }
                        //Log.d("Vaga: ", dadosEntrega.getCliente());
                    }
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            txtEntregas.setVisibility(View.GONE);

                            //
                            DadosEntregaAdapter adapter = new DadosEntregaAdapter(Principal.this, listarDados);
                            mRecyclerView.setAdapter(adapter);
                        }
                    });
                }

                if (i == 1) {
                    //
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            mRecyclerView.setVisibility(View.GONE);
                            txtEntregas.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DadosEntrega>> call, Throwable t) {
                myUpdateOperation();
                Toast.makeText(Principal.this, "Problema de acesso",
                        Toast.LENGTH_SHORT).show();

                //startActivity(new Intent(Principal.this, ErroInternet.class));
                //finish();
            }
        });
        *///CARREGAR LISTA DE ENTREGAS
        mySwipeRefreshLayout.setOnRefreshListener(this::getEntregas);

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
                myUpdateOperation();

                List<DadosEntrega> lista = response.body();

                int i = 0;
                //
                final List<DadosEntrega> listarDados = response.body();
                if (listarDados != null) {

                    for (DadosEntrega dadosEntrega : Objects.requireNonNull(lista)) {
                        if (dadosEntrega.status.equals("")) {
                            i = 1;
                        }
                        //Log.d("Vaga: ", dadosEntrega.getCliente());
                    }

                    runOnUiThread(() -> {

                        txtEntregas.setVisibility(View.GONE);

                        //
                        DadosEntregaAdapter adapter = new DadosEntregaAdapter(Principal.this, listarDados);
                        mRecyclerView.setAdapter(adapter);
                    });
                }

                if (i == 1) {
                    //
                    runOnUiThread(() -> {

                        mRecyclerView.setVisibility(View.GONE);
                        txtEntregas.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DadosEntrega>> call, Throwable t) {
                myUpdateOperation();

                //
                Toast.makeText(context, "Problema de acesso: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();

                //startActivity(new Intent(Principal.this, ErroInternet.class));
                //finish();
            }
        });
    }

    //
    private void myUpdateOperation() {
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //
            Intent i = new Intent(Principal.this, Configuracao.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            sair();
            return true;

        } else if (id == R.id.action_usuario) {
            //
            Intent i = new Intent(Principal.this, Usuario.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            sair();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sair() {
        super.finish();
    }
}
