package br.com.zenitech.siaclocalizacaocliente;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import br.com.zenitech.siaclocalizacaocliente.database.DataBaseOpenHelper;
import br.com.zenitech.siaclocalizacaocliente.domais.DadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.interfaces.IDadosEntrega;
import br.com.zenitech.siaclocalizacaocliente.repositorios.EntregasRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BroadcastReceiverAux extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiverAux";
    //
    SharedPreferences prefs;
    //
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;
    Context contexto;
    boolean verificarinternet = false;
    GPStracker gps;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BroadcastReceiverAux");
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        contexto = context;
        gps = new GPStracker(context);
        criarConexao();

        //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
        if (!Objects.requireNonNull(prefs.getString("telefone", "")).isEmpty()) {
            //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
            if (!Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {

                if (gps.isGPSEnabled()) {
                    gps.getLocation();
                }
                // PRIMEIRA VERIFICAÇÃO DE ENTREGAS (AGORA)
                getEntrega();
                entregasSemNotificacao();
                finalizarEntrega();
                // SEGUNDA VERIFICAÇÃO DE ENTREGAS (APÓS 15s)
                segundaVerificacao();

                //
                if (verificarinternet) {
                    verificarSeExisteInternet();
                    verificarinternet = false;
                    //
                    tempoParaVerificarInternet();
                }
                //
            }
        }
    }

    private void verificarSeExisteInternet() {
        if (new VerificarOnline().isOnline(contexto)) {
            Log.i("Kleilson", "Com Internet - " + new ClassAuxiliar().horaAtual());
            removeNotification(contexto);
        } else {
            Log.i("Kleilson", "Sem Internet - " + new ClassAuxiliar().horaAtual());
            new NotificarUsuario().notificar(contexto, "Atenção,", 1);
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

    // SEGUNDA VERIFICAÇÃO DE PEDIDOS
    private void segundaVerificacao() {
        //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
        if (!Objects.requireNonNull(prefs.getString("telefone", "")).isEmpty()) {
            //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
            if (!Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {
                new Handler().postDelayed(() -> {

                    getEntrega();
                    entregasSemNotificacao();
                    finalizarEntrega();
                    //terceiraVerificacao();
                    //removeNotification(contexto, 1);
                }, 30000);
            }
        }
    }

    /*
    // SEGUNDA VERIFICAÇÃO DE PEDIDOS
    private void terceiraVerificacao() {
        new Handler().postDelayed(() -> {
            getEntrega();
            entregasSemNotificacao();
            finalizarEntrega();
            //quartaVerificacao();
            removeNotification(contexto, 1);

        }, 15000);
    }

    // SEGUNDA VERIFICAÇÃO DE PEDIDOS
    private void quartaVerificacao() {
        new Handler().postDelayed(() -> {
            getEntrega();
            entregasSemNotificacao();
            finalizarEntrega();
            removeNotification(contexto, 1);

            //
            String mString = "" +
                    "\nMARCA {" + Build.BRAND + "}" +
                    "\nMODELO {" + Build.MODEL + "}" +
                    "\nVERSÃO ANDROID {" + Build.VERSION.RELEASE + "}" +
                    "\nVERSÃO SDK {" + Build.VERSION.SDK_INT + "}" +
                    "\nID {" + Build.ID + "}";

            //Log.i("Kleilson", mString);

        }, 15000);
    }
    */

    // VERIFICA SE EXISTE ENTREGAS
    private void getEntrega() {
        if (new VerificarOnline().isOnline(contexto)) {
            //final DadosEntrega dadosEntrega = entregasRepositorio.finalizarEntrega();
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
                                    contexto.startActivity(i);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                }
            });
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
        if (new VerificarOnline().isOnline(contexto)) {
            try {
                final DadosEntrega dadosEntrega = entregasRepositorio.finalizarEntrega();
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

    /*
    // VERIFICA SE EXISTE ENTREGAS
    private void entregaVisualizada(String id_pedido) {
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
        final Call<DadosEntrega> call = iEmpregos.atualizarStatus(
                prefs.getString("id_empresa", ""),
                "visualizar",
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
                            Log.i("Tag", "");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

            }
        });
    }

    // VERIFICA SE EXISTE ENTREGAS
    private void verVersaoApp() {
        try {
            final DadosEntrega dadosEntrega = entregasRepositorio.finalizarEntrega();
            final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
            final Call<DadosEntrega> call = iEmpregos.getInforEntrega(
                    prefs.getString("id_empresa", ""),
                    "inforentrega",
                    prefs.getString("telefone", ""),
                    dadosEntrega.id_pedido
            );
            call.enqueue(new Callback<DadosEntrega>() {
                @Override
                public void onResponse(@NonNull Call<DadosEntrega> call, @NonNull Response<DadosEntrega> response) {
                    if (response.isSuccessful()) {
                        DadosEntrega dados = response.body();
                        if (dados != null) {
                            entregasRepositorio.excluir(dadosEntrega.id_pedido);
                            if (dados.status.equalsIgnoreCase("P") && !dados.id_pedido.equalsIgnoreCase("0")) {
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
                                //
                                entregasRepositorio.inserir(dadosEntrega);

                                //
                                Intent i = new Intent();
                                i.setClassName("br.com.zenitech.zcallmobile", "br.com.zenitech.zcallmobile.NovaEntrega");
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.putExtra("id_pedido", dados.id_pedido);
                                i.putExtra("cliente", dados.cliente);
                                i.putExtra("localidade", dados.localidade);
                                contexto.startActivity(i);
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
    */

    // CRIA A CONEXÃO COM O BANCO DE DADOS
    private void criarConexao() {
        try {
            dataBaseOpenHelper = new DataBaseOpenHelper(contexto);
            conexao = dataBaseOpenHelper.getWritableDatabase();
            entregasRepositorio = new EntregasRepositorio(conexao);
        } catch (SQLException ex) {
            Toast.makeText(contexto, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    // SEGUNDA VERIFICAÇÃO DE PEDIDOS
    private void tempoParaVerificarInternet() {
        new Handler().postDelayed(() -> verificarinternet = false, 120000);
    }
}
