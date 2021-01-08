package br.com.zenitech.zcallmobile;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
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
import android.view.Gravity;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.Service.BatteryLevelReceiver;
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

    int conx = 0;

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

    // STATUS BATERIA
    IntentFilter ifilter;
    Intent batteryStatus;
    ImageView imgBateria, imgGPS;

    //
    TextView txtLevelBattery;
    LinearLayout statusBarCase;
    boolean verCarregando = true;
    Toolbar toolbar;
    DrawerLayout drawer;

    Button btnConfigurarSistematica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Versão: " + BuildConfig.VERSION_NAME);

        //DrawerLayout
        drawer = findViewById(R.id.drawer_layout);
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

        //
        txtLevelBattery = findViewById(R.id.txtLevelBattery);
        imgBateria = findViewById(R.id.imgBateria);
        imgGPS = findViewById(R.id.imgGPS);
        statusBarCase = findViewById(R.id.statusBarCase);
        statusBarCase.setVisibility(View.GONE);

        //VERIFICA SE O ENTREGADOR ENTROU NO PONTO
        if (Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {
            Intent i = new Intent(context, Ponto.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        //
        //drawer = findViewById(R.id.drawer_layout);

        txtEntregas = findViewById(R.id.txtEntregas);
        llSemEntrega = findViewById(R.id.llSemEntrega);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        //
        mySwipeRefreshLayout = findViewById(R.id.swiperefreshMainActivity);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mySwipeRefreshLayout.setRefreshing(true);
        mySwipeRefreshLayout.setOnRefreshListener(() -> listarS(true));

        //
        //fab.setOnClickListener(view -> listarS(true));

        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {

            fab.setImageResource(R.drawable.ic_baseline_menu);
            findViewById(R.id.fab).setOnClickListener(view -> {
                //DrawerLayout navDrawer = findViewById(R.id.drawer_layout);
                // If navigation drawer is not open yet, open it else close it.
                if (!drawer.isDrawerOpen(GravityCompat.START))
                    drawer.openDrawer(GravityCompat.START);
                else drawer.closeDrawer(GravityCompat.END);
            });
        } else {
            findViewById(R.id.fab).setOnClickListener(view -> listarS(true));
        }

        //CARREGAR LISTA DE ENTREGAS
        listarS(true);
        _salvarPosicao();

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();

        //
        temporizador();
        temporizadorMudouEntregador();

        //
        marcarComoVisto();

        /*BroadcastReceiver br = new BatteryLevelReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(br, filter);*/


        // OPÇÕES PARA QUEM USA O CASE
        usaCase();

        // CONFIGURAR SISTEMÁTICA
        btnConfigurarSistematica = findViewById(R.id.btnConfigurarSistematica);
        btnConfigurarSistematica.setOnClickListener(view -> ConfigurarSistematica());
    }

    private void ConfigurarSistematica() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();

        //
        temporizador();
        temporizadorMudouEntregador();

        // VERIFICAR SE O OPERADOR ALTEROU O STATUS DO PEDIDO
        entregasFinalizadasOperador();
        entregaMudouEntregador();
        _salvarPosicao();
        //
        marcarComoVisto();

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();

        // OPÇÕES PARA QUEM USA O CASE
        usaCase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    /*
     ******************************************************************************************
     ******************************* CLASSES AUXILIARES ***************************************
     ******************************************************************************************
     */

    private void usaCase() {
        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
            //
            //Objects.requireNonNull(getSupportActionBar()).hide();
            toolbar.setVisibility(View.GONE);
            statusBarCase.setVisibility(View.VISIBLE);

            //
            BroadcastReceiver br = new BatteryLevelReceiver();
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            this.registerReceiver(br, filter);

            // STATUS BATERIA
            ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            atualizarNivelDaBateria();
        }
    }

    private void atualizarNivelDaBateria() {
        batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        txtLevelBattery.setText(String.format("%s%%", level));

        if (verCarregando) {
            verCarregando = false;
            verificarSeEstaCarrecando();
        }

        if (BatteryLevelReceiver.StatusCarrenado) {
            imgBateria.setImageResource(R.drawable.ic_baseline_battery_charging_full_24);
        } else {
            if (level > 99)
                imgBateria.setImageResource(R.drawable.ic_battery_100);
            else if (level > 90)
                imgBateria.setImageResource(R.drawable.ic_battery_90);
            else if (level > 80)
                imgBateria.setImageResource(R.drawable.ic_battery_80);
            else if (level > 70)
                imgBateria.setImageResource(R.drawable.ic_battery_70);
            else if (level > 60)
                imgBateria.setImageResource(R.drawable.ic_battery_60);
            else if (level > 50)
                imgBateria.setImageResource(R.drawable.ic_battery_50);
            else if (level > 40)
                imgBateria.setImageResource(R.drawable.ic_battery_40);
            else if (level > 30)
                imgBateria.setImageResource(R.drawable.ic_battery_30);
            else if (level > 20)
                imgBateria.setImageResource(R.drawable.ic_battery_20);
            else if (level > 10)
                imgBateria.setImageResource(R.drawable.ic_battery_10);
            else {
                imgBateria.setImageResource(R.drawable.ic_baseline_battery_alert_24);
            }
        }

        // VERIFICA SE A ACTIVITY ESTÁ VISÍVEL
        if (VerificarActivityAtiva.isActivityVisible()) {

            new Handler().postDelayed(() -> {
                atualizarNivelDaBateria();

            }, 3000);
        }

        //Log.i(TAG, coord.getLatLon());
        if (gps.isGPSEnabled()) {
            if (gps.getLatLon().equalsIgnoreCase("0.0,0.0")) {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_searching);
                gps.getLocation();
            } else {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_on_24);
            }
        } else {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
        }
    }

    private void verificarSeEstaCarrecando() {
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            BatteryLevelReceiver.StatusCarrenado = true;
            //Toast.makeText(context, "Charging", Toast.LENGTH_LONG).show();
        } else {
            BatteryLevelReceiver.StatusCarrenado = false;
            //Toast.makeText(context,"Not Charging", Toast.LENGTH_LONG).show();
        }

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }

    private void isGPSPermisson() {
        /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }*/
        if (gps.getLocation().equalsIgnoreCase("")) {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps.getLocation();
        }
    }

    private void isGPSEnabled() {
        if (!gps.isGPSEnabled()) {
            Log.i("principal", "GPS Desativado!");
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
        } else {
            isGPSPermisson();
        }
    }

    public void listarS(boolean viewReload) {
        if (viewReload)
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

        //
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sair() {
        super.finish();
    }

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

                        if (snackbar.isShown()) {
                            snackbar.dismiss();
                        }
                    }

                    listarS(false);
                }

                /*if (prefs.getBoolean("atualizarlista", false)) {
                    listarS();
                    prefs.edit().putBoolean("atualizarlista", false).apply();
                }*/

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

    int ab = 0;
    String idb = "";

    private void entregasSemNotificacao() {
        final List<DadosEntrega> dadosEntrega = entregasRepositorio.ListentregasSemNotificacao();
        int i;
        for (i = 0; i < dadosEntrega.size(); i++) {
            ab += 1;
            idb = dadosEntrega.get(i).id_pedido;

            if (dadosEntrega.get(i).id_pedido != null) {
                entregaNotificada(idb);
            }
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

                                    //VERIFICA SE A ENTREGA JÁ FOI GRAVADA NO BANCO DE DADOS OU O PEDIDO FOI RETRNADO PARA O MESMO ENTREGADOR
                                    if (entregasRepositorio.verificarPedidoGravado(dados.id_pedido) == null || entregasRepositorio.verificarStatusPedidoGravado(dados.id_pedido).equalsIgnoreCase("EM")) {
                                        // EXCLUI A ENTREGA
                                        entregasRepositorio.excluir(dados.id_pedido);

                                        // CRIA A NOVA ENTREGA PARA SALVAR NO BANCO DE DADOS
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
    //gfgdfhhgh
    private void entregaNotificada(final String id_pedido) {
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
        final Call<DadosEntrega> call = iEmpregos.atualizarStatus(
                prefs.getString("id_empresa", ""),
                "notificado_r",
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
                                        listarS(true);
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

    int abc = 0;
    String idbc = "";

    // VERIFICAR ENTREGAS FINALIZADAS PELO OPERADOR P/ EXCLUIR DO APP
    private void entregasFinalizadasOperador() {
        if (new VerificarOnline().isOnline(context)) {
            final List<DadosEntrega> dadosEntrega = entregasRepositorio.entregasFinalizadasOperador();
            int i;
            for (i = 0; i < dadosEntrega.size(); i++) {
                abc += 1;
                idbc = dadosEntrega.get(i).id_pedido;
                if (dadosEntrega.get(i).id_pedido != null) {
                    try {

                        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                        final Call<DadosEntrega> call = iEmpregos.entregasFinalizadasOperador(
                                prefs.getString("id_empresa", ""),
                                "entregasFinalizadasOperador",
                                prefs.getString("telefone", ""),
                                idbc
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
                                                    idbc,
                                                    dados.status,
                                                    dados.nome_atendente
                                            );
                                            //entregasRepositorio.excluir(dadosEntrega.id_pedido);
                                            listarS(true);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                            }
                        });

                    } catch (Exception ignored) {

                    }
                }
            }
        }
    }

    int a = 0;
    String id = "";

    //
    private void entregaMudouEntregador() {
        if (new VerificarOnline().isOnline(context)) {
            final List<DadosEntrega> dadosEntrega = entregasRepositorio.ListaEntregaMudouEntregador();

            int i;
            for (i = 0; i < dadosEntrega.size(); i++) {
                a += 1;
                id = dadosEntrega.get(i).id_pedido;
                try {
                    if (dadosEntrega.get(i).id_pedido != null) {
                        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                        final Call<DadosEntrega> call = iEmpregos.entregaMudouEntregador(
                                prefs.getString("id_empresa", ""),
                                "entregaMudouEntregador",
                                prefs.getString("telefone", ""),
                                dadosEntrega.get(i).id_pedido
                        );
                        call.enqueue(new Callback<DadosEntrega>() {
                            @Override
                            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                                if (response.isSuccessful()) {
                                    DadosEntrega dados = response.body();

                                    if (dados != null) {
                                        Log.i("LEMudouEntregador", id + " | " + dados.status + " | " + dados.id_pedido);
                                        if (dados.status.equalsIgnoreCase("EM")) {

                                            //Log.i("LEMudouEntregador", id + " | " + dados.status);//dadosEntrega.status
                                            entregasRepositorio._entregasOperadorMudouEntregador(
                                                    dados.id_pedido,
                                                    dados.status,
                                                    dados.nome_atendente
                                            );
                                            //entregasRepositorio.excluir(dadosEntrega.id_pedido);
                                            listarS(true);
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

            /*
            final DadosEntrega dadosEntrega = entregasRepositorio.EntregaMudouEntregador();
            try {
                if (dadosEntrega.id_pedido != null) {
                    final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                    final Call<DadosEntrega> call = iEmpregos.entregasFinalizadasOperador(
                            prefs.getString("id_empresa", ""),
                            "entregaMudouEntregador",
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
                                        listarS(true);
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

             */
        }
    }

    public void marcarComoVisto() {
        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.marcarComoVisto(
                prefs.getString("id_empresa", ""),
                "marcarComoVisto",
                prefs.getString("telefone", "")
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {

                if (response.isSuccessful()) {
                    DadosEntrega dados = response.body();
                    //
                    if (dados != null) {
                        if (dados.status.equals("OK")) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DadosEntrega> call, Throwable t) {
            }
        });
    }

    private void temporizadorMudouEntregador() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            new Handler().postDelayed(() -> {

                //VERIFICA SE O APARELHO ESTÁ CONECTADO A INTERNET
                if (online.isOnline(context)) {
                    entregaMudouEntregador();
                    entregasFinalizadasOperador();
                }

                //CHAMA O TEMPORIZADOR NOVAMENTE
                temporizadorMudouEntregador();
            }, 5000);

        }
    }

}
