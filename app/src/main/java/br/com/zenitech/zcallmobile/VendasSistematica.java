package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.SistematicaRepositorio;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendasSistematica extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //
    ArrayList<String> listaFormasPagamento;
    ArrayList<String> listaProdutos;
    //
    private Spinner spFormasPagamento;
    private Spinner spProdutos;

    private Context context;
    private SharedPreferences prefs;
    ClassAuxiliar aux;
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    SistematicaRepositorio sistematicaRepositorio;

    //
    EditText etPreco, etQuantidade;
    Button btnSalvar;

    private SpotsDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendas_sistematica);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        aux = new ClassAuxiliar();
        criarConexao();
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .setCancelable(false)
                .build();
        listaFormasPagamento = sistematicaRepositorio.getFormasPagamento();
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item, listaFormasPagamento);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spFormasPagamento = findViewById(R.id.spFormasPagamento);
        spFormasPagamento.setAdapter(adapter);
        spFormasPagamento.setOnItemSelectedListener(VendasSistematica.this);

        //
        listaProdutos = sistematicaRepositorio.getProdutos();
        ArrayAdapter adapterProdutos = new ArrayAdapter(this, R.layout.spinner_item, listaProdutos);
        adapterProdutos.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spProdutos = findViewById(R.id.spProdutos);
        spProdutos.setAdapter(adapterProdutos);
        spProdutos.setOnItemSelectedListener(VendasSistematica.this);

        //
        etQuantidade = findViewById(R.id.etQuantidade);
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new MoneyTextWatcher(etPreco));

        //
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(view -> salvar());

        findViewById(R.id.fab).setOnClickListener(view -> finish());
    }

    private void salvar() {
        if (spProdutos.getSelectedItem().toString().equalsIgnoreCase("PRODUTO")) {
            Toast.makeText(context, "Selecione um produto.", Toast.LENGTH_LONG).show();
        } else if (spFormasPagamento.getSelectedItem().toString().equalsIgnoreCase("FORMA DE PAGAMENTO")) {
            Toast.makeText(context, "Selecione uma forma de pagamento.", Toast.LENGTH_LONG).show();
        } else if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0") || etPreco.getText().toString().equals("") || etPreco.getText().toString().equals("R$ 0,00")) {
            Toast.makeText(context, "Quantidade e Preço não podem ser vazios.", Toast.LENGTH_LONG).show();
        } else {

            //barra de progresso pontos
            dialog.show();

            // VERIFICA SE TEM INTERNET
            if (new VerificarOnline().isOnline(context)) {
                //
                final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
                //Log.i("Sistematica", String.valueOf(sistematicaRepositorio.IdFormaPagamento(spFormasPagamento.getSelectedItem().toString())));
                //
                final Call<DadosEntrega> call = iEmpregos.vedaSistematica(
                        "venda_sistematica",
                        "" + prefs.getString("id_empresa", ""),
                        "" + prefs.getString("telefone", ""),
                        "" + sistematicaRepositorio.IdProduto(spProdutos.getSelectedItem().toString()),
                        "" + sistematicaRepositorio.IdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                        "" + aux.converterValores(etPreco.getText().toString()),
                        "" + aux.inserirDataAtual(),
                        "" + aux.horaAtual(),
                        "" + etQuantidade.getText().toString()
                );

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {

                        DadosEntrega dados = response.body();
                        //msg(dados.status);
                        if (dados.status.equals("OK")) {
                            msg("Operação realizada com sucesso!");
                        } else {
                            salvarOffLine();
                        }

                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {
                        salvarOffLine();
                    }
                });
            } else {
                salvarOffLine();
            }
        }
    }

    private void msg(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private void salvarOffLine() {
        sistematicaRepositorio.salvarVendaSistematica(
                sistematicaRepositorio.IdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                sistematicaRepositorio.IdProduto(spProdutos.getSelectedItem().toString()),
                etQuantidade.getText().toString(),
                String.valueOf(aux.converterValores(etPreco.getText().toString()))
        );

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        msg("Operação realizada com sucesso! Pedido Salvo OffLine!");
        finish();
    }

    private void criarConexao() {
        try {
            //
            dataBaseOpenHelper = new DataBaseOpenHelper(context);
            //
            conexao = dataBaseOpenHelper.getWritableDatabase();
            //
            sistematicaRepositorio = new SistematicaRepositorio(conexao, aux);
            //Toast.makeText(context, "Conexão criada com sucesso!", Toast.LENGTH_LONG).show();
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        /*String[] fPag = spFormasPagamento.getSelectedItem().toString().split(" _ ");
        if (fPag[1].equals("A PRAZO")) {

            runOnUiThread(() -> {
                //tilDocumento.setVisibility(View.VISIBLE);
                tilVencimento.setVisibility(View.VISIBLE);
            });

            if (fPag[3].equals("1")) {

                runOnUiThread(() -> tilDocumento.setVisibility(View.VISIBLE));
            }
        } else {
            runOnUiThread(() -> {
                tilDocumento.setVisibility(View.GONE);
                tilVencimento.setVisibility(View.GONE);
                txtVencimentoFormaPagamentoReceber.setText(cAux.exibirDataAtual());

                //Log.i("ContasReceber", cAux.exibirDataAtual());
                //Log.i("ContasReceber", txtVencimentoFormaPagamentoReceber.getText().toString());
            });
        }*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[^0-9]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }
}