package br.com.zenitech.zcallmobile;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.adapters.DadosEntregaAdapter;
import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Principal2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //
    SharedPreferences prefs;
    private Context context;

    private SwipeRefreshLayout mySwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Snackbar snackbar;
    View sbView;
    private TextView textView;
    LinearLayout llSemEntrega;

    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;

    TextView txtEntregas;
    private VerificarOnline online;
    RelatarErros relatarErros;

    // -------------
    boolean verificarinternet = true;

    // -------------
    GPStracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Versão: " + BuildConfig.VERSION_NAME);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        relatarErros = new RelatarErros();

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        gps = new GPStracker(context);
        online = new VerificarOnline();
        FloatingActionButton fab = findViewById(R.id.fab);
        snackbar = Snackbar.make(fab, "", Snackbar.LENGTH_INDEFINITE).setAction("Action", null);
        sbView = snackbar.getView();
        textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);

        //VERIFICA SE O ENTREGADOR ENTROU NO PONTO
        if (Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {
            Intent i = new Intent(context, Ponto.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        txtEntregas = findViewById(R.id.txtEntregas);
        llSemEntrega = findViewById(R.id.llSemEntrega);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        //
        mySwipeRefreshLayout = findViewById(R.id.swiperefreshMainActivity);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mySwipeRefreshLayout.setRefreshing(true);
        mySwipeRefreshLayout.setOnRefreshListener(
                this::listarS
        );

        //
        fab.setOnClickListener(view -> listarS());

        findViewById(R.id.fab).setOnClickListener(view -> listarS());

        //CARREGAR LISTA DE ENTREGAS
        listarS();
        //_salvarPosicao();

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();

        //
        temporizador();

        // VERIFICAR SE O OPERADOR ALTEROU O STATUS DO PEDIDO
        entregasFinalizadasOperador();
        _salvarPosicao();

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    private void isGPSPermisson() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }

    private void isGPSEnabled() {
        if (!gps.isGPSEnabled()) {
            Log.i("principal", "GPS Desativado!");
        }else{
            isGPSPermisson();
        }
    }

    public void listarS() {

        mySwipeRefreshLayout.setRefreshing(true);
        criarConexao();

        entregasRepositorio = new EntregasRepositorio(conexao);
        final List<DadosEntrega> dados = entregasRepositorio.ListaEntregas();
        //
        runOnUiThread(() -> {
            if (dados.size() != 0) {
                //myUpdateOperation();
                mRecyclerView.setVisibility(View.VISIBLE);
                llSemEntrega.setVisibility(View.GONE);

                //
                DadosEntregaAdapter adapter = new DadosEntregaAdapter(context, dados);
                adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);
            } else {
                //myUpdateOperation();
                mRecyclerView.setVisibility(View.GONE);
                llSemEntrega.setVisibility(View.VISIBLE);

            }
        });

        if (!Objects.requireNonNull(prefs.getString("telefone", "")).isEmpty()) {

            if (!Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {

                getEntrega();
                entregasSemNotificacao();
                finalizarEntrega();

                if (verificarinternet) {
                    verificarSeExisteInternet();
                    verificarinternet = false;
                }
            }
        }
    }

    // SALVAR A POSIÇÃO DO ENTREGADOR
    private void _salvarPosicao() {
        // INICIA A CLASS GPS
        gps = new GPStracker(context);

        // VERIFICA SE O GPS ESTÁ ATIVO
        if (gps.isGPSEnabled()) {
            gps.getLocation();
            //gps.getLatLon();
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

    //
    private void myUpdateOperation() {
        mySwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_usuario) {
            Intent i = new Intent(context, Usuario.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            sair();
        } else if (id == R.id.nav_configuracoes) {
            Intent i = new Intent(context, Configuracao.class);
            i.putExtra("principal", "sim");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            sair();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sair() {
        super.finish();
    }

    int conx = 0;

    private void temporizador() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            new Handler().postDelayed(() -> {

                //VERIFICA SE O APARELHO ESTÁ CONECTADO A INTERNET
                if (!online.isOnline(context)) {

                    if (conx == 0) {
                        //
                        textView.setTextColor(Color.RED);
                        textView.setText(R.string.atencao_sem_internet);
                        snackbar.show();

                        conx = 1;
                    }
                } else {

                    if (conx == 1) {
                        conx = 0;


                        textView.setTextColor(Color.GREEN);
                        textView.setText(R.string.reconectando);

                        listarS();

                        if (snackbar.isShown()) {
                            snackbar.dismiss();
                        }
                    }
                }

                if (prefs.getBoolean("atualizarlista", false)) {
                    listarS();
                    prefs.edit().putBoolean("atualizarlista", false).apply();
                }

                //CHAMA O TEMPORIZADOR NOVAMENTE
                temporizador();
            }, 2000);

        }
    }

    private void verificarSeExisteInternet() {
        if (new VerificarOnline().isOnline(context)) {
            Log.i("Kleilson", "Com Internet - " + new ClassAuxiliar().horaAtual());
            removeNotification(context);
        } else {
            Log.i("Kleilson", "Sem Internet - " + new ClassAuxiliar().horaAtual());
            new NotificarUsuario().notificar(context, "Atenção,", 1);
        }
    }

    public void removeNotification(Context context) {

        NotificationManager nMgr = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
    }

    private void entregasSemNotificacao() {
        String id_pedido = entregasRepositorio.entregasSemNotificacao();
        if (!id_pedido.equalsIgnoreCase("")) {
            entregaNotificada(id_pedido);
        }
    }

    // VERIFICA SE EXISTE ENTREGAS
    private void getEntrega() {
        if (new VerificarOnline().isOnline(context)) {
            try {
                //
                final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                final Call<DadosEntrega> call = iEmpregos.getInforEntrega(
                        prefs.getString("id_empresa", ""),
                        "inforentrega",
                        prefs.getString("telefone", ""),
                        ""
                );
                call.enqueue(new Callback<DadosEntrega>() {
                    @Override
                    public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                        if (response.isSuccessful()) {
                            DadosEntrega dados = response.body();
                            if (dados != null) {
                                if (dados.status.equalsIgnoreCase("P") && !dados.id_pedido.equalsIgnoreCase("0")) {

                                    //VERIFICA SE A ENTREGA JÁ FOI GRAVADA NO BANCO DE DADOS
                                    if (entregasRepositorio.verificarPedidoGravado(dados.id_pedido) == null) {
                                        DadosEntrega dadosEntrega = new DadosEntrega();
                                        dadosEntrega.id_pedido = dados.id_pedido;
                                        dadosEntrega.hora_recebimento = dados.hora_recebimento;
                                        dadosEntrega.nome_atendente = dados.nome_atendente;
                                        dadosEntrega.telefone_pedido = dados.telefone_pedido;
                                        dadosEntrega.status = dados.status;
                                        dadosEntrega.troco_para = dados.troco_para;
                                        dadosEntrega.valor = dados.valor;
                                        dadosEntrega.id_cliente = dados.id_cliente;
                                        dadosEntrega.cliente = dados.cliente;
                                        dadosEntrega.apelido = dados.apelido;
                                        dadosEntrega.endereco = dados.endereco;
                                        dadosEntrega.localidade = dados.localidade;
                                        dadosEntrega.numero = dados.numero;
                                        dadosEntrega.complemento = dados.complemento;
                                        dadosEntrega.ponto_referencia = dados.ponto_referencia;
                                        dadosEntrega.coord_latitude = dados.coord_latitude;
                                        dadosEntrega.coord_longitude = dados.coord_longitude;
                                        dadosEntrega.produtos = dados.produtos;
                                        dadosEntrega.brindes = dados.brindes;
                                        dadosEntrega.observacao = dados.observacao;
                                        dadosEntrega.forma_pagamento = dados.forma_pagamento;
                                        dadosEntrega.ativar_btn_ligar = dados.ativar_btn_ligar;
                                        entregasRepositorio.inserir(dadosEntrega);

                                        //
                                        entregaNotificada(dados.id_pedido);

                                        //
                                        Intent i = new Intent();
                                        i.setClassName("br.com.zenitech.zcallmobile", "br.com.zenitech.zcallmobile.NovaEntrega");
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        i.putExtra("id_pedido", dados.id_pedido);
                                        i.putExtra("cliente", dados.cliente);
                                        i.putExtra("localidade", dados.localidade);
                                        context.startActivity(i);
                                    }
                                }
                            }

                            myUpdateOperation();
                        } else {
                            myUpdateOperation();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {
                        myUpdateOperation();
                    }
                });
            } catch (Exception ignored) {
                //myUpdateOperation();
            }
        } else {

            myUpdateOperation();
        }
    }

    // ATUALIZA O STATUS DA ENTREGA PARA NOTIFICADA
    private void entregaNotificada(final String id_pedido) {
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
        final Call<DadosEntrega> call = iEmpregos.atualizarStatus(
                prefs.getString("id_empresa", ""),
                "notificado",
                prefs.getString("telefone", ""),
                id_pedido
        );
        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                if (response.isSuccessful()) {
                    DadosEntrega dados = response.body();
                    if (dados != null) {
                        if (dados.status.equalsIgnoreCase("OK")) {
                            entregasRepositorio.entregaNotificada(id_pedido);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

            }
        });
    }

    // FINALIZAR ENTREGAS
    private void finalizarEntrega() {
        if (new VerificarOnline().isOnline(context)) {
            final DadosEntrega dadosEntrega = entregasRepositorio.finalizarEntrega();
            try {
                if (dadosEntrega.id_pedido != null) {
                    final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                    final Call<DadosEntrega> call = iEmpregos.finalizarEntrega(
                            prefs.getString("id_empresa", ""),
                            "finalizar_entrega",
                            prefs.getString("telefone", ""),
                            dadosEntrega.id_pedido,
                            dadosEntrega.coord_latitude,
                            dadosEntrega.coord_longitude
                    );
                    call.enqueue(new Callback<DadosEntrega>() {
                        @Override
                        public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                            if (response.isSuccessful()) {
                                DadosEntrega dados = response.body();
                                if (dados != null) {
                                    if (dados.status.equalsIgnoreCase("OK")) {

                                        entregasRepositorio.excluir(dadosEntrega.id_pedido);
                                        listarS();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                        }
                    });
                }
            } catch (Exception ignored) {

            }
        }
    }

    // VERIFICAR ENTREGAS FINALIZADAS PELO OPERADOR P/ EXCLUIR DO APP
    private void entregasFinalizadasOperador() {
        if (new VerificarOnline().isOnline(context)) {
            final DadosEntrega dadosEntrega = entregasRepositorio.entregasFinalizadasOperador();
            try {
                if (dadosEntrega.id_pedido != null) {
                    final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                    final Call<DadosEntrega> call = iEmpregos.entregasFinalizadasOperador(
                            prefs.getString("id_empresa", ""),
                            "entregasFinalizadasOperador",
                            prefs.getString("telefone", ""),
                            dadosEntrega.id_pedido
                    );
                    call.enqueue(new Callback<DadosEntrega>() {
                        @Override
                        public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                            if (response.isSuccessful()) {
                                DadosEntrega dados = response.body();
                                if (dados != null) {
                                    if (!dados.status.equalsIgnoreCase("NULO")) {

                                        Log.i("KLE", "OK!");//dadosEntrega.status
                                        entregasRepositorio._entregasFinalizadasOperador(
                                                dadosEntrega.id_pedido,
                                                dados.status,
                                                dados.nome_atendente
                                        );
                                        //entregasRepositorio.excluir(dadosEntrega.id_pedido);
                                        listarS();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                        }
                    });
                }
            } catch (Exception ignored) {

            }
        }
    }

}
