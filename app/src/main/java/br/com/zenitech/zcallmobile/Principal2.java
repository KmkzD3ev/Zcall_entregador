package br.com.zenitech.zcallmobile;

import static br.com.zenitech.zcallmobile.ConfigApp.vrsaoPOS;
import static br.com.zenitech.zcallmobile.NotificarUsuario.cancel;
import static br.com.zenitech.zcallmobile.NotificarUsuario.notificar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.adapters.DadosContatosAdapter;
import br.com.zenitech.zcallmobile.adapters.DadosEntregaAdapter;
import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaFormPag;
import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaProdutos;
import br.com.zenitech.zcallmobile.domais.DadosContatos;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IConfigurarSistematica;
import br.com.zenitech.zcallmobile.interfaces.IDadosContatos;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import br.com.zenitech.zcallmobile.repositorios.SistematicaRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Principal2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SharedPreferences prefs;
    private Context context;
   // private ScreenBrightnessManager setBrightness;

    private SwipeRefreshLayout mySwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView rcContatos;
    private Snackbar snackbar;
    View sbView;
    private TextView textView;
    LinearLayout llSemEntrega, llContatos;
    LinearLayoutCompat llSistematica;
    AlertDialog alerta;

    int conx = 0;

    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;
    SistematicaRepositorio sistematicaRepositorio;

    TextView txtEntregas;
    private VerificarOnline online;
    RelatarErros relatarErros;

    // -------------
    GPStracker gps;
    ImageView imgGPS;

    //
    TextView versaoApp, telEntregador, txtStatusGps;
    LinearLayout statusBarCase;
    LinearLayoutCompat llProtecaoTela;
    Toolbar toolbar;
    DrawerLayout drawer;

    Button btnConfigurarSistematica, btnSistematica;
    ClassAuxiliar aux;
    FloatingActionButton fab;
    CoordinatorLayout principal2;

    //
    private boolean protecaotela = false;
    private int timePT = 0;

    boolean fp = false;
    boolean pr = false;

    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Versão: " + BuildConfig.VERSION_NAME);

        //
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*//DrawerLayout
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        relatarErros = new RelatarErros();

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        aux = new ClassAuxiliar();
        gps = GPStracker.getInstance(context);
        online = new VerificarOnline();
        fab = findViewById(R.id.fab);
        snackbar = Snackbar.make(fab, "", Snackbar.LENGTH_INDEFINITE).setAction("Action", null);
        sbView = snackbar.getView();
        textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
       // setBrightness = setBrightness;

        //
        principal2 = findViewById(R.id.principal2);

        versaoApp = findViewById(R.id.versaoApp);
        versaoApp.setText(String.format("Versão %s", BuildConfig.VERSION_NAME));
        telEntregador = findViewById(R.id.telEntregador);
        telEntregador.setText(aux.mask(prefs.getString("telefone", "")));
        txtStatusGps = findViewById(R.id.txtStatusGps);

        //
        imgGPS = findViewById(R.id.imgGPS);
        statusBarCase = findViewById(R.id.statusBarCase);
        statusBarCase.setVisibility(View.GONE);

        //
        llSistematica = findViewById(R.id.llSistematica);
        btnSistematica = findViewById(R.id.btnSistematica);

        //
        llProtecaoTela = findViewById(R.id.llProtecaoTela);
        llProtecaoTela.setOnClickListener(view -> resetProtecaoTela());

        //
        btnSistematica.setOnClickListener(view -> {
            Intent i = new Intent(context, VendasSistematica.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

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


        // Contatos
        llContatos = findViewById(R.id.llContatos);
        rcContatos = findViewById(R.id.rcContatos);
        rcContatos.setLayoutManager(new LinearLayoutManager(context));
        findViewById(R.id.fabContatos).setOnClickListener(view -> llContatos.setVisibility(View.GONE));

        //
        mySwipeRefreshLayout = findViewById(R.id.swiperefreshMainActivity);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mySwipeRefreshLayout.setRefreshing(true);
        mySwipeRefreshLayout.setOnRefreshListener(() -> listarEntregasOff(true));//listarS(true)

        //
        //fab.setOnClickListener(view -> listarS(true));

        //if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
        //if (vrsaoPOS) {
        fab.setImageResource(R.drawable.ic_baseline_menu);
        fab.setOnClickListener(v -> startActivity(new Intent(this, MenuApp.class)));

        // CRIAR CONEXÃO COM O BANCO DE DADOS DO APP
        criarConexao();

        // OPÇÕES PARA QUEM USA O CASE
        //usaCase();
        btnConfigurarSistematica = findViewById(R.id.btnConfigurarSistematica);
        btnConfigurarSistematica.setOnClickListener(view -> ConfigurarSistematica());

        // CONFIGURAR SISTEMÁTICA
        if (prefs.getString("configSistematica", "0").equalsIgnoreCase("1")) {
            llSistematica.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            btnSistematica.setVisibility(View.VISIBLE);

        } else {
            ConfigurarSistematica();
            fab.setVisibility(View.GONE);
            btnSistematica.setVisibility(View.GONE);
        }
    }

    private void resetProtecaoTela() {
        if (vrsaoPOS) {
            //setBrightness(25);
            llProtecaoTela.setVisibility(View.GONE);
            protecaotela = false;
            timePT = 0;
        }
    }

    // CONFIGURA O APP PARA USAR VENDAS SISTEMÁTICA
    // BUSCA INFORMAÇÕES DE FORMA DE PAGAMENTO E PRODUTOS PARA ADINICONAR NO BANCO DE DADOS

    private void ConfigurarSistematica() {
        Log.i("Principal", "Configurando...");
        if (new VerificarOnline().isOnline(context)) {
            btnConfigurarSistematica.setBackgroundResource(R.drawable.botao_customizado);
            btnConfigurarSistematica.setEnabled(false);
            btnConfigurarSistematica.setText("INCIANDO POS...");

            sistematicaRepositorio = new SistematicaRepositorio(conexao, aux);

            try {
                sistematicaRepositorio.excluir();
            } catch (Exception e) {
                Log.e("Principal", e.getMessage());
            }

            // FORMAS DE PAGAMENTO
            try {
                //
                final IConfigurarSistematica iConfigurarSistematica = IConfigurarSistematica.retrofit.create(IConfigurarSistematica.class);
                final Call<List<DadosConfigSistematicaFormPag>> call = iConfigurarSistematica.FormasPagamento(
                        prefs.getString("id_empresa", ""),
                        "forms_pagamento"
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<DadosConfigSistematicaFormPag>> call, @NonNull Response<List<DadosConfigSistematicaFormPag>> response) {
                        if (response.isSuccessful()) {
                            List<DadosConfigSistematicaFormPag> lista = response.body();
                            if (lista != null) {
                                for (DadosConfigSistematicaFormPag dados : Objects.requireNonNull(lista)) {
                                    Log.i("Principal", dados.id_forma_pagamento + " | " + dados.forma_pagamento);
                                    sistematicaRepositorio.inserirFormasPagamento(dados.id_forma_pagamento, dados.forma_pagamento);
                                    fp = true;
                                    finalizarConfiguracao();
                                }
                            } else {
                                erroInicioPOS();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DadosConfigSistematicaFormPag>> call, @NonNull Throwable t) {
                        Log.i("Principal", t.getMessage());
                        erroInicioPOS();
                    }
                });
            } catch (Exception e) {
                Log.i("Principal", e.getMessage());
                erroInicioPOS();
            }

            // PRODUTOS
            try {
                //
                final IConfigurarSistematica iConfigurarSistematica = IConfigurarSistematica.retrofit.create(IConfigurarSistematica.class);
                final Call<List<DadosConfigSistematicaProdutos>> call = iConfigurarSistematica.Produtos(
                        prefs.getString("id_empresa", ""),
                        "produtos"
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<DadosConfigSistematicaProdutos>> call, @NonNull Response<List<DadosConfigSistematicaProdutos>> response) {
                        Log.i("Principal2", response.toString());
                        if (response.isSuccessful()) {
                            List<DadosConfigSistematicaProdutos> lista = response.body();
                            if (lista != null) {
                                for (DadosConfigSistematicaProdutos dados : Objects.requireNonNull(lista)) {
                                    Log.i("Principal2", dados.id_produto + " | " + dados.produto);
                                    sistematicaRepositorio.inserirProdutos(dados.id_produto, dados.produto);
                                    pr = true;
                                    finalizarConfiguracao();
                                }
                            }
                        } else {
                            erroInicioPOS();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DadosConfigSistematicaProdutos>> call, @NonNull Throwable t) {
                        Log.e("Principal", t.getMessage());
                        erroInicioPOS();
                    }
                });
            } catch (Exception e) {
                Log.e("Principal", e.getMessage());
                erroInicioPOS();
            }


        }
    }

    private void erroInicioPOS() {
        btnConfigurarSistematica.setBackgroundResource(R.drawable.botao_finalizar_entrega);
        btnConfigurarSistematica.setEnabled(true);
        btnConfigurarSistematica.setText("INCIAR POS");
    }

    private void finalizarConfiguracao() {
        if (fp && pr) {
            prefs.edit().putString("configSistematica", "1").apply();
        }

        if (prefs.getString("configSistematica", "0").equalsIgnoreCase("1")) {
            llSistematica.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            btnSistematica.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
            btnSistematica.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();


        // CRIAR CONEXÃO COM O BANCO DE DADOS DO APP
        criarConexao();

        listarEntregasOff(true);

        // Verificar se o GPS foi aceito pelo entregador
        isGPSEnabled();

        // OPÇÕES PARA QUEM USA O CASE
        usaCase();

        // Oculta a lista de contatos
        llContatos.setVisibility(View.GONE);

        //
        resetProtecaoTela();

        temporizador();
    }

   /*public void setBrightness(int brightness) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                // Do stuff here
                //constrain the value of brightness
                if (brightness < 0)
                    brightness = 0;
                else if (brightness > 255)
                    brightness = 255;

                ContentResolver cResolver = this.getApplicationContext().getContentResolver();
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();

        listarEntregasOff(false);
    }

    /*
     ******************************************************************************************
     ******************************* CLASSES AUXILIARES ***************************************
     ******************************************************************************************
     */

    private void usaCase() {
        //if(vrsaoPOS) {
            toolbar.setVisibility(View.GONE);
            statusBarCase.setVisibility(View.VISIBLE);
            atualizarGPS();
        //}
    }

    private void atualizarGPS() {
        if (gps.isGPSEnabled()) {
            if (gps.getLatLon().equalsIgnoreCase("0.0,0.0")) {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_searching);
                gps.getLocation();
                txtStatusGps.setText("Buscando localização...");
            } else {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_on_24);
                txtStatusGps.setTextSize(8);
                String[] latlon = gps.getLatLon().split(",");
                txtStatusGps.setText(String.format("Lat: %s\nLon: %s", latlon[0], latlon[1]));//String.format("%s", gps.getLatLon())
            }
        } else {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
            txtStatusGps.setText("GPS Desativado!");
        }
    }

    private void isGPSPermisson() {
        /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }*/
        if (gps.getLocation().equalsIgnoreCase("")) {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
            //gps.opcoesContato(Principal2.this);
            if (i == 0) {
                i = 1;
                a();
            }
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps.getLocation();
        }
    }

    private void isGPSEnabled() {
        if (!gps.isGPSEnabled()) {
            Log.i("principal", "GPS Desativado!");
            Toast.makeText(context, "A ativação do GPS é necessaria para o uso do App.", Toast.LENGTH_LONG).show();
            //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), null);

            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
        } else {
            isGPSPermisson();
        }
    }

    //
    private void criarConexao() {
        try {
            dataBaseOpenHelper = new DataBaseOpenHelper(context);
            conexao = dataBaseOpenHelper.getWritableDatabase();
            entregasRepositorio = new EntregasRepositorio(conexao);
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
        super.onBackPressed();
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
        } else if (id == R.id.nav_contacts) {
            getContatos();

        } else if (id == R.id.nav_reset_app) {
            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        //
        drawer.closeDrawer(GravityCompat.START);
        return true;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();

                    Cursor cur = getContentResolver().query(contactData, null, null, null, null);
                    if (cur.getCount() > 0) {// thats mean some resutl has been found
                        if (cur.moveToNext()) {
                            String id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                            //String imagem = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                            Log.e("ZCall Id", id);
                            Log.e("ZCall Names", name);
                            //Log.e("ZCall Imagem", imagem);
                            String phoneNumber = "";
                            if (Integer.parseInt(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                                while (phones.moveToNext()) {
                                    //String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.e("ZCall Number", phoneNumber);

                                }
                                phones.close();
                            }
                            opcoesContato(id, name, phoneNumber);
                        }
                    }
                    cur.close();
                }
                break;
        }

    }

    private void sair() {
        super.finish();
    }

    /*public void removeNotification(Context context) {

        NotificationManager nMgr = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nMgr != null) {
            nMgr.cancelAll();
        }
    }*/

    private void TemInternet() {
        //VERIFICA SE O APARELHO ESTÁ CONECTADO A INTERNET
        if (!online.isOnline(context)) {

            if (conx == 0) {
                //
                textView.setTextColor(Color.RED);
                textView.setText(R.string.atencao_sem_internet);
                snackbar.show();
                notificar(context, "Atenção,", 1);
                conx = 1;
            }
        } else {

            if (conx == 1) {
                conx = 0;

                //removeNotification(context);
                cancel(context);
                textView.setTextColor(Color.GREEN);
                textView.setText(R.string.reconectando);

                if (snackbar.isShown()) {
                    snackbar.dismiss();
                }
            }
        }
    }

    public void listarEntregasOff(boolean viewReload) {
        if (viewReload)
            mySwipeRefreshLayout.setRefreshing(true);
        List<DadosEntrega> dados = entregasRepositorio.ListaEntregas();
        //
        runOnUiThread(() -> {
            if (dados.size() != 0) {
                myUpdateOperation();
                mRecyclerView.setVisibility(View.VISIBLE);
                llSemEntrega.setVisibility(View.GONE);

                //
                DadosEntregaAdapter adapter = new DadosEntregaAdapter(context, dados, conexao, entregasRepositorio);
                adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);
            } else {
                myUpdateOperation();
                mRecyclerView.setVisibility(View.GONE);
                llSemEntrega.setVisibility(View.VISIBLE);

            }
        });
    }

    // TEMPO PARA ATUALIZAR AS INFORMAÇÕES DO APP
    private void temporizador() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            //
            new Handler(Looper.myLooper()).postDelayed(() -> {
                if (prefs.getString("ponto", "").equalsIgnoreCase("ok")) {
                    Log.e("PRINCIPAL", "LOOP PRINCIPAL");

                    listarEntregasOff(false);
                    //gps.enviarDados();

                    if (!protecaotela && vrsaoPOS) {// prefs.getString("usa_case", "0").equalsIgnoreCase("1")
                        timePT++;
                        if (timePT > 10) {
                            timePT = 0;
                            llProtecaoTela.setVisibility(View.VISIBLE);
                            //setBrightness(0);
                            protecaotela = true;

                            llProtecaoTela.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                        }
                    }

                    //
                    atualizarGPS();

                    if (VerificarActivityAtiva.isActivityVisible()) {
                        TemInternet();
                    }

                    //CHAMA O TEMPORIZADOR NOVAMENTE
                    temporizador();
                }
            }, 30000);
            //
        }
    }

    // *********************************************************************************************
    // ****************************** CONTATOS DO TELEFONE *****************************************
    // *********************************************************************************************

    private void opcoesContato(String id, String nome, String telefone) {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_zcall_mobile);
        //define o titulo
        builder.setTitle("Contato");
        //define a mensagem
        builder.setMessage("Id: " + id + " | Nome: " + nome + " | Telefone: " + telefone);
        //define um botão como negativo.
        builder.setNegativeButton("Editar", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
        });
        builder.setNeutralButton("Cancelar", (arg0, arg1) -> {
        });
        //define um botão como positivo
        builder.setPositiveButton("Ligar", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();

        });

        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    public void a() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Principal2.this);
        //builder.setIcon(R.drawable.logo_zcall_mobile);
        //define o titulo
        //builder.setTitle("O ZCall Mobile,");
        //define a mensagem
        builder.setMessage("O ZCall Mobile, coleta dados de localização a fim de informar para central, qual entregador está mais próximo do endereço do pedido.");
        //define um botão como negativo.
        /*builder.setNegativeButton("Editar", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
        });*/
        builder.setNeutralButton("Sair", (arg0, arg1) -> {
        });
        //define um botão como positivo
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
            dialogInterface.cancel();
            ActivityCompat.requestPermissions(Principal2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }/* (arg0, arg1) -> {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }*/);

        //cria o AlertDialog
        builder.create();
        //Exibe alerta
        builder.show();
    }
}
