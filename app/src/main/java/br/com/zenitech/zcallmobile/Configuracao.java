package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.domais.SmsDomains;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.ISms;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Configuracao extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private Context context;
    private ClassAuxiliar aux;
    private EditText etIdEmpresa, etNumero;
    private LinearLayout llNB, llEnv;
    private String codigo, numero, id_empresa;

    int veioDoPrincipal = 0;
    int veioDoSplash = 0;

    //
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    GPStracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        /*Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Zcall Mobile");
        getSupportActionBar().setSubtitle("Configurações");*/

        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        aux = new ClassAuxiliar();

        criarConexao();
        gps = GPStracker.getInstance(context);
        gps.isGPSEnabled();
        gps.opcoesContato(this);

        //
        llNB = findViewById(R.id.llNB);
        llEnv = findViewById(R.id.llEnv);

        //
        etIdEmpresa = findViewById(R.id.etIdEmpresa);
        etIdEmpresa.setText(prefs.getString("id_empresa", ""));

        //
        etNumero = findViewById(R.id.etNumero);
        etNumero.setText(prefs.getString("telefone", ""));
        etNumero.addTextChangedListener(Mask.insert("(##)#####-####", etNumero));
        etNumero.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                //ESCODER O TECLADO
                // TODO Auto-generated method stub
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                //
                validarCampos();

                handled = true;
            }
            return handled;
        });

        findViewById(R.id.btnSalvarConfiguracoes).setOnClickListener(view -> {
            //
            validarCampos();
            /*
            if (etNumero.getText().toString().equals("")) {
                Toast.makeText(getBaseContext(), "Informe seu celular!", Toast.LENGTH_LONG).show();
            } else {

                //
                llNB.setVisibility(View.GONE);

                //
                SalvarCelular();
            }
            */
        });

        //RECEBE O NÚMERO DO CELULAR INFORMADO
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {

                try {
                    if (Objects.requireNonNull(params.getString("principal")).equals("sim")) {
                        veioDoPrincipal = 1;
                    }

                    if (Objects.requireNonNull(params.getString("splash")).equals("sim")) {
                        veioDoSplash = 1;
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void validarCampos() {
        //
        if (etIdEmpresa.getText().toString().equals("")) {
            Toast.makeText(getBaseContext(), "Informe o código da licença!", Toast.LENGTH_LONG).show();
        } else if (etNumero.getText().toString().equals("")) {
            Toast.makeText(getBaseContext(), "Informe seu celular!", Toast.LENGTH_LONG).show();
        } else {
            //
            llNB.setVisibility(View.GONE);

            //
            SalvarCelular();
        }
    }

    private void SalvarCelular() {
        //
        //String numero = etNumero.getText().toString();
        numero = aux.soNumeros(etNumero.getText().toString());
        id_empresa = aux.soNumeros(etIdEmpresa.getText().toString());
        //prefs.edit().putString("telefone", numero).apply();

        TextView txtNC = findViewById(R.id.txtNC);
        txtNC.setText(etNumero.getText().toString());

        // PARA TESTES INTERNOS
        if (numero.equals("84996116068"))
            codigo = "123456";
        else
            codigo = aux.getRandomNumber(6, 0, 9);

        //Mostrar código pra teste
        //Toast.makeText(context, codigo, Toast.LENGTH_LONG).show();

        llEnv.setVisibility(View.VISIBLE);

        //
        final IDadosEntrega iDadosEntrega = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iDadosEntrega.confirmarTelefone(
                id_empresa,
                "confirmartelefone",
                numero
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {


                //
                final DadosEntrega dados = response.body();

                //
                if (dados != null) {

                    if (dados.status.contains("ok")) {

                        prefs.edit().putString("id_empresa", id_empresa).apply();

                        // Define se o entregador usa o case
                        prefs.edit().putString("usa_case", dados.confirmado).apply();

                        // Define se o entregador usa o case
                        prefs.edit().putString("localizar", dados.localizar).apply();

                        EnviarSms();
                        /*
                        // SÓ PRA TESTE
                        Toast.makeText(context, codigo, Toast.LENGTH_LONG).show();
                        Sair();
                        // FIM SÓ PRA TESTE
                        */
                    } else {
                        //
                        Toast.makeText(context, "Não foi possível confirmar seu telefone!", Toast.LENGTH_LONG).show();
                        llEnv.setVisibility(View.GONE);
                        llNB.setVisibility(View.VISIBLE);
                    }
                } else {
                    //
                    Toast.makeText(context, "Não foi possível confirmar seu telefone!", Toast.LENGTH_LONG).show();
                    llEnv.setVisibility(View.GONE);
                    llNB.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {
                Toast.makeText(context, "Erro " + t.getMessage(), Toast.LENGTH_LONG).show();
                //Log.i("zcall", t.getMessage());

                llEnv.setVisibility(View.GONE);
                llNB.setVisibility(View.VISIBLE);
            }
        });

        //
        //Sair();
    }

    private void EnviarSms() {
        //Nova barra de progresso
        //dialog.show();

        //
        final ISms iSms = ISms.retrofit.create(ISms.class);

        //
        final Call<SmsDomains> call = iSms.Sms(
                etIdEmpresa.getText().toString(),
                numero,
                codigo
        );

        call.enqueue(new Callback<SmsDomains>() {
            @Override
            public void onResponse(@NonNull Call<SmsDomains> call, @NonNull Response<SmsDomains> response) {


                //
                final SmsDomains dados = response.body();

                //
                if (dados != null) {

                    if (dados.getStatus().contains("ok")) {
                        //
                        Toast.makeText(context, "Sms enviado com sucesso!", Toast.LENGTH_LONG).show();

                        Sair();
                    }
                } else {
                    //
                    Toast.makeText(context, "Erro ao enviar o sms!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SmsDomains> call, @NonNull Throwable t) {
                Toast.makeText(context, "Erro " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Sair();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //
        Sair();
        super.onBackPressed();
    }

    private void Sair() {

        /*
        Intent i = new Intent(context, Principal2.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        */

        //SE VEIO DA TELA PRINCIPAL
        if (veioDoPrincipal == 1) {
            Intent i = new Intent(context, Principal2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            finish();
        }
        //SE VEIO DA TELA DE SPLASHSCREEN
        else if (veioDoSplash == 1) {
            finish();
        } else {
            String cel = aux.soNumeros(etNumero.getText().toString());

            Bundle params = new Bundle();
            params.putString("celular", cel);
            params.putString("codigo", codigo);

            Intent i = new Intent(context, ConfirmarTelefone.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtras(params);
            startActivity(i);

            finish();
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
            //megaSenaRepositorio = new MegaSenaRepositorio(conexao);
            //Toast.makeText(context, "Conexão criada com sucesso!", Toast.LENGTH_LONG).show();
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }
}
