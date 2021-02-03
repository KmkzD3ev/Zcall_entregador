package br.com.zenitech.zcallmobile;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.SphericalUtil;

import java.util.Locale;
import java.util.Objects;

import br.com.zenitech.zcallmobile.Service.BatteryLevelReceiver;
import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalizarEntrega extends AppCompatActivity {
    //
    private static final String TAG = "FinalizarEntrega";
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 1;

    //
    private Uri uriContact;
    private String contactID;
    String id_pedido = "";
    String telefone, enderecoGM;
    private SharedPreferences prefs;
    private Context context;
    private SpotsDialog dialog;
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;
    VerificarOnline online;
    Snackbar snackbar;
    TextView textView, txtLevelBattery;
    RelatarErros relatarErros;
    double coord_latitude_pedido = 0;
    double coord_longitude_pedido = 0;
    GPStracker coord;
    Button btnFinalizarEntrega;
    LatLng posicaoInicial;
    LatLng posicaiFinal;
    double distance;
    LinearLayout statusBarCase;

    // DADOS CLIENTE
    String idCliente;
    double coordCliLat, coordCliLon;

    // STATUS BATERIA
    IntentFilter ifilter;
    Intent batteryStatus;
    ImageView imgBateria, imgGPS;

    //
    int i = 0;
    boolean verCarregando = true;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalizar_entrega);
        toolbar = findViewById(R.id.toolbarFinalizaEntrega);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Atendente");

        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;

        //
        coord = new GPStracker(context);
        criarConexao();
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .setCancelable(false)
                .build();
        online = new VerificarOnline();
        FloatingActionButton fab = findViewById(R.id.fab);
        snackbar = Snackbar
                .make(fab, "", Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null);
        View sbView = snackbar.getView();
        textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);

        txtLevelBattery = findViewById(R.id.txtLevelBattery);
        imgBateria = findViewById(R.id.imgBateria);
        imgGPS = findViewById(R.id.imgGPS);
        statusBarCase = findViewById(R.id.statusBarCase);
        statusBarCase.setVisibility(View.GONE);

        relatarErros = new RelatarErros();

        //
        VerificarActivityAtiva.activityResumed();

        //
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {

                id_pedido = params.getString("id_pedido");
                idCliente = params.getString("id_cliente");
                coordCliLat = params.getDouble("coordCliLat");
                coordCliLon = params.getDouble("coordCliLon");

                //
                Objects.requireNonNull(getSupportActionBar()).setSubtitle(params.getString("nome_atendente"));

                //
                TextView tvNumeroPedido = findViewById(R.id.txtNumeroPedido);
                tvNumeroPedido.setText(params.getString("id_pedido"));

                //
                TextView tvCliente = findViewById(R.id.txtNomeCliente);
                tvCliente.setText(String.format("%s / %s", params.getString("cliente"), !Objects.requireNonNull(params.getString("apelido")).equalsIgnoreCase("") ? params.getString("apelido") : "Sem apelido"));

                //
                TextView tvTelefone = findViewById(R.id.txtTelefone);
                tvTelefone.setText(params.getString("telefone_pedido"));
                telefone = params.getString("telefone_pedido");
                if (Objects.requireNonNull(prefs.getString("ativar_btn_ligar", "0")).equalsIgnoreCase("1")) {

                    tvTelefone.setText(params.getString("telefone_pedido"));
                    telefone = params.getString("telefone_pedido");
                } else {
                    tvTelefone.setText("**********");
                }

                //
                String endereco = params.getString("endereco") + ", Nº " + params.getString("numero") +
                        ", " + params.getString("complemento") + " - " + params.getString("localidade");

                TextView tvEndereco = findViewById(R.id.txtEnderecoCliente);
                tvEndereco.setText(endereco);
                enderecoGM = endereco;

                //
                TextView tvPontoReferencia = findViewById(R.id.txtPontoReferencia);
                tvPontoReferencia.setText((!Objects.requireNonNull(params.getString("ponto_referencia")).equals("") ? params.getString("ponto_referencia") : "Não informado"));

                //
                //TextView tvProdutos = findViewById(R.id.txtProdutos);
                //tvProdutos.setText(fromHtml(params.getString("produtos")));
                WebView wvProdutos = findViewById(R.id.wvProdutos);
                wvProdutos.loadDataWithBaseURL(null, params.getString("produtos"), "text/html", "utf-8", null);

                //
                TextView tvValor = findViewById(R.id.txtValor);
                tvValor.setText(String.format("R$ %s", params.getString("valor")));

                //
                TextView tvBrindes = findViewById(R.id.txtBrindes);
                tvBrindes.setText(params.getString("brindes"));

                //
                TextView tvTroco = findViewById(R.id.txtTroco);
                tvTroco.setText((!Objects.requireNonNull(params.getString("troco_para")).equals("0,00") ? "R$ " + params.getString("troco_para") : "Não precisa"));

                //
                TextView tvObservacao = findViewById(R.id.txtObservacao);
                tvObservacao.setText(params.getString("observacao"));

                //
                TextView tvFormaPagamento = findViewById(R.id.txtFormaPagamento);
                tvFormaPagamento.setText(params.getString("forma_pagamento"));

                entregasRepositorio.entregaConfirmada(id_pedido);
            }
        }

        fab.setOnClickListener(view -> {
            /*
            //
            VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                Intent i = new Intent(context, Chat.class);
                startActivity(i);
            }
            */

            /*String texto = "Estamos trabalhando por aqui! Liberamos em breve.";
            Toast.makeText(context, texto, Toast.LENGTH_LONG).show();*/

            //
            Intent i = new Intent(context, Principal2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            //
            finish();
        });

        //findViewById(R.id.btnFinalizarEntrega).setOnClickListener(view -> temporizador());
        findViewById(R.id.btnFinalizarEntrega).setOnClickListener(view -> verifCordenadas());

        findViewById(R.id.btnCancelarEntrega).setOnClickListener(view -> {

            //
            //VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                finalizarPedido(id_pedido, "C");
            }
        });

        btnFinalizarEntrega = findViewById(R.id.btnFinalizarEntrega);
        btnFinalizarEntrega.setVisibility(View.VISIBLE);
        btnFinalizarEntrega.setEnabled(false);

        // Verificar se o GPS foi aceito pelo entregador
        isGPSPermisson();

        // OPÇÕES PARA QUEM USA O CASE
        usaCase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();

        // Verificar se o GPS foi aceito pelo operador
        isGPSEnabled();
        /*if (coord.isGPSEnabled()) {
            coord.getLocation();
            //gps.getLatLon();
        }*/


        //
        //temporizador();

        // OPÇÕES PARA QUEM USA O CASE
        usaCase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                new GPStracker(context).getLocation();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finalizar_entrega, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            case R.id.action_rota:

                //Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
                //Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                //mapIntent.setPackage("com.google.android.apps.maps");

                //Uri gmmIntentUri = Uri.parse("geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California");
                Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s", enderecoGM));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                break;
            case R.id.action_excluir:
                cancelarPedido(id_pedido);
                break;
            case R.id.action_ligar_central:
                Uri call;
                if (Objects.requireNonNull(prefs.getString("ativar_btn_ligar", "0")).equalsIgnoreCase("1")) {
                    call = Uri.parse(String.format("tel:%s", telefone));
                } else {
                    call = Uri.parse(String.format("tel:%s", ""));
                }
                Intent surf = new Intent(Intent.ACTION_DIAL, call);
                startActivity(surf);
                //"tel:" + telefone +  using native contacts selection
                // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
                //startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            //retrieveContactName();
            //retrieveContactPhoto();
            String telefone = retrieveContactNumber();

            Uri uri = Uri.parse("tel:" + telefone);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);

            startActivity(intent);

        }

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Todas as alterações necessárias foram feitas
                        coord.getLocation();
                        //verifCordenadas();
                        break;
                    case Activity.RESULT_CANCELED:
                        // O usuário cancelou o dialog, não fazendo as alterações requeridas
                        Toast.makeText(FinalizarEntrega.this, "Operação cancelada!", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(FinalizarEntrega.this, Principal2.class));
        finish();
        super.onBackPressed();
    }

    /*
     ******************************************************************************************
     ******************************* CLASSES AUXILIARES ***************************************
     ******************************************************************************************
     */

    private void isGPSEnabled() {
        if (!coord.isGPSEnabled()) {
            Log.i("principal", "GPS Desativado!");
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
        } else {
            isGPSPermisson();
        }
    }

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
        } else {
            btnFinalizarEntrega.setEnabled(true);
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

        // || batteryStatus.getBooleanExtra(BatteryManager.EXTRA_STATUS, false)
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

        Log.i(TAG, coord.getLatLon());
        if (coord.isGPSEnabled()) {
            if (coord.getLatLon().equalsIgnoreCase("0.0,0.0")) {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_searching);
                coord.getLocation();
                btnFinalizarEntrega.setEnabled(false);
            } else {
                imgGPS.setImageResource(R.drawable.ic_baseline_location_on_24);
                btnFinalizarEntrega.setEnabled(true);
            }
        } else {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
            btnFinalizarEntrega.setEnabled(false);
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
        if (coord.getLocation().equalsIgnoreCase("")) {
            imgGPS.setImageResource(R.drawable.ic_baseline_location_off_24);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            coord.getLocation();
        }
    }

    public void finalizarPedido(final String id_pedido, String status) {

        //barra de progresso pontos
        //dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.getFinalizarEntrega(
                prefs.getString("id_empresa", ""),
                "finalizarentrega",// finalizarentregateste
                id_pedido,
                status,
                coord_latitude_pedido,
                coord_longitude_pedido
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {


                int code = response.code();

                try {

                    if (response.isSuccessful()) {

                        DadosEntrega dados = response.body();

                        if (dados != null && dados.status.equals("OK")) {
                            //
                            msg("Entrega Finalizada!");

                            //
                            entregasRepositorio.excluir(id_pedido);
                        }

                    } else {
                        //
                        enviarErro("Falha" + code);
                        //
                        msg("Aguardando sincronismo!");

                        //
                        entregasRepositorio.alterar(id_pedido, coord_latitude_pedido, coord_longitude_pedido);
                    }
                } catch (Exception ignored) {

                }

                //
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                //
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                //
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                //
                enviarErro(t.getMessage());

                //
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                //
                //Toast.makeText(context, "Encontramos um problema e já estamos verificando o que aconteceu!",
                //        Toast.LENGTH_SHORT).show();
                //
                entregasRepositorio.alterar(id_pedido, coord_latitude_pedido, coord_longitude_pedido);

                //
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                //
                finish();
            }
        });
    }

    public void cancelarPedido(final String id_pedido) {
        //barra de progresso pontos
        dialog.show();
        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
        //
        final Call<DadosEntrega> call = iEmpregos.getCancelarEntrega(
                prefs.getString("id_empresa", ""),
                "cancelarentrega",
                id_pedido
        );
        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                if (response.isSuccessful()) {
                    DadosEntrega dados = response.body();
                    if (dados != null && dados.status.equals("OK")) {
                        //
                        msg("Entrega Cancelada!");
                        //
                        entregasRepositorio.excluir(id_pedido);
                        //
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        //
                        Intent i = new Intent(context, Principal2.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //
                        finish();
                    } else {
                        msg("Esta entrega não pode ser cancelada, contate o atendente.");
                    }
                } else {
                    msg("Não foi possível cancelar esta entrega, tente novamente mais tarde.");
                }
                //
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {
                //
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                //
                Toast.makeText(context, "Não foi possível cancelar esta entrega, tente novamente mais tarde.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void atualizarLocalCliente() {

        //msg(coord_latitude_pedido + " / " + coord_longitude_pedido);

        //barra de progresso pontos
        //dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.atualizarLocalCliente(
                prefs.getString("id_empresa", ""),
                "atualizar_local_cliente",
                idCliente,
                coord_latitude_pedido,
                coord_longitude_pedido
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {

                try {
                    if (response.isSuccessful()) {

                        DadosEntrega dados = response.body();

                        if (dados != null && dados.status.equals("OK")) {
                            coordCliLat = coord_latitude_pedido;
                            coordCliLon = coord_longitude_pedido;

                            msg("Salvou localização do cliente");
                            //
                            verifRaio();
                        } else {
                            msg("Erro aqui");
                        }
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {
            }
        });
    }

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

    private String retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (Objects.requireNonNull(cursorID).moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (Objects.requireNonNull(cursorPhone).moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Log.d(TAG, "Contact Phone Number: " + contactNumber);

        return contactNumber;
    }

    public boolean raio(double latIni, double lonIni) {
        boolean result = false;

        posicaoInicial = new LatLng(latIni, lonIni);
        try {
            posicaiFinal = new LatLng(coordCliLat, coordCliLon);
        } catch (Exception ignored) {
            posicaiFinal = new LatLng(0.0, 0.0);
        }
        distance = SphericalUtil.computeDistanceBetween(posicaoInicial, posicaiFinal);

        //Log.e("LOG", "A Distancia é = " + (distance));
        Locale mL = new Locale("pt", "BR");
        //String.format(mL, "%4.3f%s", distance, unit);
        String unit = "m";
        if (distance >= 1000) {
            distance /= 1000;
            unit = "km";
        }
        Log.e("Distancia", "A Distancia é = " + String.format(mL, "%4.3f%s", distance, unit));

        if (distance <= 150) {
            result = true;
        } else {
            msg("Você parece estar a uns " + String.format(mL, "%4.3f%s", distance, unit) + " de onde o cliente está. Chegue mais perto para finalizar a entrega!");
        }

        return result;
    }

    /**
     * P1: PEGA AS CORDENADAS
     * P2: VERIFICAR SE TEM INTERNET
     * P3: TEM INTERNET! VERIFICA SE O CLIENTE JÁ POSSUI AS CORDENADAS DA SUA CASA
     * P3.1: CLIENTE NÃO TEM COREDENAS! ATUALIZA O CADASTRO COM AS CORDENAS INFORMADA ANTERIORMENTE
     * P4: CALCULAR O RAIO DA CASA DO CLIENTE COM A POSIÇÃO DO ENTREGADOR
     */

    // PEGAR AS CORDENADAS DO ENTREGADOR
    private void verifCordenadas() {
        //barra de progresso pontos
        dialog.show();

        //msg(String.valueOf(coord_latitude_pedido));
        // VERIFICA SE A ACTIVITY ESTÁ VISÍVEL
        if (VerificarActivityAtiva.isActivityVisible()) {

            //
            String[] c = coord.getLatLon().split(",");
            coord_latitude_pedido = Double.parseDouble(c[0]);
            coord_longitude_pedido = Double.parseDouble(c[1]);

            // VERIFICA SE AS CORDENADAS DO ENTREGADOR FORAM RECONHECIDAS
            if (coord_latitude_pedido != 0.0) {

                //msg("Peguei a latitude: " + coord_latitude + ", " + coord_longitude);
                verifClienteCordenada();
            }

            new Handler().postDelayed(() -> {

                // VERIFICA SE AS CORDENADAS DO ENTREGADOR FORAM RECONHECIDAS
                if (coord_latitude_pedido != 0.0) {

                    //msg("Peguei a latitude: " + coord_latitude_pedido);
                    verifClienteCordenada();
                } else {
                    i++;

                    if (i < 50) {
                        verifCordenadas();
                    } else {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }

            }, 500);
        }
    }

    // VERIFICAR AS CORDENADAS DO CLIENTE
    private void verifClienteCordenada() {

        //msg("Cordenadas do cliente: " + coordCliLat);

        if (coordCliLat == 0.0) {
            if (online.isOnline(context)) {
                //msg("Com internet!");

                // ATUALIZA A LOCALIZAÇÃO DO CLIENTE
                atualizarLocalCliente();
            } else {
                //msg("Sem intenet!");

                // DEFINE A LOCALIZAÇÃO DO CLIENTE COM A CORDENADA ATUAL DO ENTREGADOR
                coordCliLat = coord_latitude_pedido;
                coordCliLon = coord_longitude_pedido;

                //
                verifRaio();
            }
        } else {

            //
            verifRaio();
        }
    }

    //
    private void verifRaio() {
        if (raio(coord_latitude_pedido, coord_longitude_pedido)) {

            // FINALIZA O PEDIDO
            finalizarPedido(id_pedido, "E");
        } else {
            //msg("Você parece não está próximo ao cliente! Tente novamente.");

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void msg(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public void enviarErro(String erro) {
        // CRIAR A MENSAGEM DE ERRO
        StringBuilder msg;
        msg = new StringBuilder();
        msg.append("ID_Empresa: ").append(prefs.getString("id_empresa", "")).append(" <br> ");
        msg.append("Usuário: ").append(prefs.getString("telefone", "")).append(" <br> ");
        msg.append("Erro: ").append(erro);

        // ENVIAR ERRO
        relatarErros.enviarErro(msg.toString());

        finish();
    }
}
