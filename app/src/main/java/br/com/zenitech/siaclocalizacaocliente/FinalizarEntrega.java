package br.com.zenitech.siaclocalizacaocliente;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.SphericalUtil;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;

import br.com.zenitech.siaclocalizacaocliente.database.DataBaseOpenHelper;
import br.com.zenitech.siaclocalizacaocliente.domais.DadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.interfaces.IDadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.repositorios.EntregasRepositorio;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalizarEntrega extends AppCompatActivity {
    private static final String TAG = FinalizarEntrega.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
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
    TextView textView;
    RelatarErros relatarErros;
    double coord_latitude_pedido = 0;
    double coord_longitude_pedido = 0;
    GPStracker coord;
    Button btnFinalizarEntrega;
    LatLng posicaoInicial;
    LatLng posicaiFinal;
    double distance;

    // DADOS CLIENTE
    String idCliente;
    double coordCliLat, coordCliLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalizar_entrega);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Atendente");

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
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

        relatarErros = new RelatarErros();

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
                getSupportActionBar().setSubtitle(params.getString("nome_atendente"));

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

            String texto = "Estamos trabalhando por aqui! Liberamos em breve.";
            Toast.makeText(context, texto, Toast.LENGTH_LONG).show();

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

        // Verificar se o GPS foi aceito pelo entregador
        isGPSPermisson();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();

        //
        //temporizador();
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
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(FinalizarEntrega.this, Principal2.class));
        finish();
        super.onBackPressed();
    }

    private void isGPSPermisson() {
        if (coord.getLocation().equalsIgnoreCase("")) {
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
        }else{
            msg("Você parece estar a uns " + String.format(mL, "%4.3f%s", distance, unit) +" de onde o cliente está. Chegue mais perto para finalizar a entrega!");
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
            new Handler().postDelayed(() -> {

                String[] c = coord.getLatLon().split(",");
                coord_latitude_pedido = Double.valueOf(c[0]);
                coord_longitude_pedido = Double.valueOf(c[1]);

                // VERIFICA SE AS CORDENADAS DO ENTREGADOR FORAM RECONHECIDAS
                if (coord_latitude_pedido != 0.0) {

                    //msg("Peguei a latitude: " + coord_latitude_pedido);
                    verifClienteCordenada();
                } else {
                    verifCordenadas();
                }

            }, 3000);
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
