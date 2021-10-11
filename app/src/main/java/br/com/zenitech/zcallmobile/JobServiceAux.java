package br.com.zenitech.zcallmobile;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceAux extends JobService {
    private static final String TAG = "JobServiceAux";

    // -------------
    SharedPreferences prefs;
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    EntregasRepositorio entregasRepositorio;
    Context contexto;
    boolean verificarinternet = false;
    GPStracker gps;
    // -------------

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onStartJob(JobParameters params) {

        //
        Log.i(TAG, "onStartJob(" + params.getExtras().getString("string") + ")");

        //
        contexto = this;
        gps = GPStracker.getInstance(contexto);
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        criarConexao();

        // Inicia
        new MyAsyncTask(this).execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        boolean jobCancelled = true;
        return false;
    }

    // INNER CLASS
    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<JobParameters, Void, String> {

        private JobServiceAux jss;

        MyAsyncTask(JobServiceAux j) {
            jss = j;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected String doInBackground(JobParameters... jobParameters) {
            Log.d(TAG, "doInBackground()");

            if (gps.isGPSEnabled()) {
                gps.getLocation();
            }

            String answer = "Chegou até aqui";

            //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
            if (!Objects.requireNonNull(prefs.getString("telefone", "")).isEmpty()) {
                //SE O TELEFONE DO USUÁRIO FOR VASIO ENTRA
                if (!Objects.requireNonNull(prefs.getString("ponto", "")).isEmpty()) {
                    // PRIMEIRA VERIFICAÇÃO DE ENTREGAS (AGORA)
                    getEntrega();
                    entregasSemNotificacao();
                    finalizarEntrega();

                    //
                    if (verificarinternet) {
                        verificarSeExisteInternet();
                        verificarinternet = false;
                    }
                }
            }

            jss.jobFinished(jobParameters[0], false);
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute");
            //EventBus.getDefalt().post(new Message(s));
            super.onPostExecute(s);
        }
    }

    private void verificarSeExisteInternet() {
        if (new VerificarOnline().isOnline(contexto)) {
            Log.i("Kleilson", "Com Internet - " + new ClassAuxiliar().horaAtual());
            removeNotification(contexto, 1);
        } else {
            Log.i("Kleilson", "Sem Internet - " + new ClassAuxiliar().horaAtual());
            new NotificarUsuario().notificar(contexto, "Atenção,", 1);
        }
    }

    public void removeNotification(Context context, int notificationId) {
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
        /*String id_pedido = entregasRepositorio.entregasSemNotificacao();
        if (!id_pedido.equalsIgnoreCase("")) {
            entregaNotificada(id_pedido);
        }*/
    }

    // VERIFICA SE EXISTE ENTREGAS
    private void getEntrega() {
        if (new VerificarOnline().isOnline(contexto)) {
            try {
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
                                        if(!ConfigApp.vrsaoPOS) {
                                            entregaNotificada(dados.id_pedido);
                                        }

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
            } catch (Exception ignored) {

            }
        }
    }

    // ATUALIZA O STATUS DA ENTREGA PARA NOTIFICADA
    private void entregaNotificada(final String id_pedido) {
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
        String opcao = "notificado_r";
        if(ConfigApp.vrsaoPOS){
            opcao = "notificado_pos";
        }
        final Call<DadosEntrega> call = iEmpregos.atualizarStatus(
                prefs.getString("id_empresa", ""),
                opcao,
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
                           // entregasRepositorio.entregaNotificada(id_pedido);
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
}
