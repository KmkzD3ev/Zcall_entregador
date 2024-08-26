package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaFormPag;
import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaProdutos;
import br.com.zenitech.zcallmobile.interfaces.IConfigurarSistematica;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import br.com.zenitech.zcallmobile.repositorios.SistematicaRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuApp extends AppCompatActivity {
    SharedPreferences prefs;
    private Context context;
    ClassAuxiliar aux;
    private VerificarOnline online;

    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;
    SistematicaRepositorio sistematicaRepositorio;

    boolean pr = false, fp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        aux = new ClassAuxiliar();
        online = new VerificarOnline();

        findViewById(R.id.btn_menu_entregador).setOnClickListener(v -> {
            Intent i = new Intent(this, Usuario.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_menu_contatos).setOnClickListener(v -> {
            Intent i = new Intent(this, Contatos.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_menu_reset).setOnClickListener(v -> {

            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_menu_sistematica).setOnClickListener(v -> ConfigurarSistematica());

        findViewById(R.id.fabMenu).setOnClickListener(v -> {
            //
            Intent i = new Intent(context, Principal2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            //
            super.finish();
        });

        // CRIAR CONEXÃO COM O BANCO DE DADOS DO APP
        criarConexao();
    }

    private void ConfigurarSistematica() {
        Log.i("Principal", "Configurando...");
        if (new VerificarOnline().isOnline(context)) {

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
                                }
                                
                                fp = true;
                                finalizarConfiguracao();
                            } else {
                                Log.e("Principal", "ERROR");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DadosConfigSistematicaFormPag>> call, @NonNull Throwable t) {
                        Log.i("Principal", t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.i("Principal", e.getMessage());
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

                                }

                                pr = true;
                                finalizarConfiguracao();
                            }
                        } else {
                            Log.e("Principal", "ERROR");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<DadosConfigSistematicaProdutos>> call, @NonNull Throwable t) {
                        Log.e("Principal", t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e("Principal", e.getMessage());
            }
        }
    }

    private void finalizarConfiguracao() {
        
        if(pr && fp){
            Toast.makeText(context, "Configuração finalizada!", Toast.LENGTH_LONG).show();
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
}