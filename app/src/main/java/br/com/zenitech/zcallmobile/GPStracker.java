package br.com.zenitech.zcallmobile;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.domais.DadosPosicoes;
import br.com.zenitech.zcallmobile.domais.DadosVendasSistematica;
import br.com.zenitech.zcallmobile.domais.JSonZCall;
import br.com.zenitech.zcallmobile.interfaces.IDadosZCall;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import br.com.zenitech.zcallmobile.repositorios.PosicoesRepositorio;
import br.com.zenitech.zcallmobile.repositorios.SistematicaRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPStracker {

    //
    public SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    public EntregasRepositorio entregasRepositorio;
    PosicoesRepositorio posicoesRepositorio;
    SistematicaRepositorio sistematicaRepositorio;
    public SharedPreferences prefs;
    Context context;
    //
    private static final String TAG = "GPStracker";
    private double lat = 0.0;
    private double lon = 0.0;

    List<DadosPosicoes> dados;

    List<DadosEntrega> pedidos;
    List<DadosPosicoes> posicoes;
    List<DadosVendasSistematica> sistematicas;

    //
    public static GPStracker instance;
    public static boolean TemPedido = false;

    public List<String> IdPedMudEntregador;

    private GPStracker(Context c) {
        context = c.getApplicationContext();
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        IdPedMudEntregador = new ArrayList<>(List.of(""));

        criarConexao();
        temporizador();
    }

    public static GPStracker getInstance(Context context) {
        if (instance == null) {
            instance = new GPStracker(context);
        }
        return instance;
    }

    String getLocation() {

        try {

            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.d(TAG, "onStatusChanged: " + s);
                }

                @Override
                public void onProviderEnabled(String s) {
                    Log.d(TAG, "onProviderEnabled: " + s);
                }

                @Override
                public void onProviderDisabled(String s) {
                    Log.d(TAG, "onProviderDisabled: " + s);
                }
            };

            if (this.isGPSEnabled()) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
            }

            return lat + "," + lon;

        } catch (Exception ignored) {

        }

        return "";
    }

    String getLatLon() {
        return lat + "," + lon;
    }

    // RETORNA SE O GPS ESTÁ ATIVO OU INATIVO
    boolean isGPSEnabled() {
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // CHAMADO EM PRINCIPAL2.CLASS
    public void enviarDados() {

        // VERIFICA SE TEM INTERNET
        if (new VerificarOnline().isOnline(context)) {
            try {
                for (String ped : IdPedMudEntregador) {
                    Log.i(TAG, "KLEILSON: " + ped);
                }


                //prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
                boolean localizarEntregador = prefs.getString("localizar", "0").equalsIgnoreCase("1");

                pedidos = entregasRepositorio.ListaEntregas();
                if (localizarEntregador)
                    posicoes = posicoesRepositorio.ListaPosicoes();
                sistematicas = sistematicaRepositorio.getVendasSistematica();

                JSonZCall jSonZCall = new JSonZCall();// = new JSonZCall(null, null, null, null, null, null, null, null);
                jSonZCall.id_empresa = prefs.getString("id_empresa", "");
                jSonZCall.opcao = "setData";
                jSonZCall.telefone = prefs.getString("telefone", "");
                jSonZCall.latitude = String.format("%s", lat);
                jSonZCall.longitude = String.format("%s", lon);
                jSonZCall.Pedidos = pedidos;
                if (localizarEntregador)
                    jSonZCall.Posicoes = posicoes;
                jSonZCall.Sistematicas = sistematicas;

                //
                final IDadosZCall iDadosZCall = IDadosZCall.retrofit.create(IDadosZCall.class);
                final Call<JSonZCall> call = iDadosZCall.enviarDados(jSonZCall);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<JSonZCall> call, @NonNull Response<JSonZCall> response) {
                        if (response.isSuccessful()) {
                            JSonZCall dados = response.body();
                            if (dados != null) {

                                TemPedido = false;

                                // SE O ENTREGADOR FOR LOCALIZAVÉL FAZ AS VALIDAÇÕES
                                if (localizarEntregador) {
                                    try {
                                        if (lat != 0.0 && lon != 0.0) {
                                            // POSIÇÃO ATUAL: SE RETORNAR 0, A POSIÇÃO NÃO FOI GRAVADA ONLINE
                                            if (dados.retorno_posicao != null) {
                                                if (dados.retorno_posicao.equalsIgnoreCase("0")) {
                                                    _inserirPosOffLine();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception: " + e.getMessage());
                                    }

                                    try {
                                        // RECEBE OS IDS DAS POSIÇÕES SALVAS WEB E APAGA DO BANCO INTERNO
                                        if (dados.retorno_posicao_off != null) {
                                            for (DadosPosicoes pos : dados.retorno_posicao_off) {
                                                Log.i(TAG, "ID_POSIÇÕES_OFF:" + pos.id);
                                                posicoesRepositorio.excluir(pos.id);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception: " + e.getMessage());
                                    }
                                }


                                try {
                                    // RECEBE OS IDS DAS POSIÇÕES SALVAS WEB E APAGA DO BANCO INTERNO
                                    if (dados.retorno_sistematicas != null) {
                                        for (DadosVendasSistematica sis : dados.retorno_sistematicas) {
                                            Log.i(TAG, "ID_SISTEMATICAS:" + sis.id);
                                            sistematicaRepositorio.deleteVendasSistematica(sis.id);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE OS IDS DOS PEDIDOS FINALIZADOS PELO ENTREGADOR E SALVO
                                    // NA WEB PARA APAGAR DO BANCO INTERNO
                                    if (dados.retorno_pedidos_fin != null) {
                                        for (DadosEntrega pedFin : dados.retorno_pedidos_fin) {
                                            Log.i(TAG, "ID_ENT_FINALIZADA:" + pedFin.id_pedido);
                                            entregasRepositorio.excluir(pedFin.id_pedido);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE OS IDS DOS PEDIDOS FINALIZADOS PELO OPERADOR
                                    if (dados.retorno_pedidos_operador != null) {
                                        for (DadosEntrega ped : dados.retorno_pedidos_operador) {
                                            Log.i(TAG, "ID_ENT_EDI_OPERADOR:" + ped.id_pedido);

                                            entregasRepositorio._entregasFinalizadasOperador(ped.id_pedido, ped.status, ped.nome_atendente);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE OS IDS DOS PEDIDOS FINALIZADOS PELO OPERADOR
                                    if (dados.retorno_pedidos_mudou_entregador != null) {
                                        for (DadosEntrega ped : dados.retorno_pedidos_mudou_entregador) {
                                            Log.i(TAG, "ID_ENT_MUDOU:" + ped.id_pedido);

                                            entregasRepositorio._entregasOperadorMudouEntregador(ped.id_pedido, ped.status, ped.nome_atendente);

                                            // VERIFICA SE O PEDIDO JÁ FOI NOTIFICADO
                                            if (!IdPedMudEntregador.contains(ped.id_pedido)) {
                                                TemPedido = true;
                                                IdPedMudEntregador.add(ped.id_pedido);
                                            }

                                            //TemPedido = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE OS IDS DOS PEDIDOS NOTIFICADOS
                                    if (dados.retorno_pedidos_notificados != null) {
                                        for (DadosEntrega ped : dados.retorno_pedidos_notificados) {
                                            Log.i(TAG, "ID_NOTIFICADO:" + ped.id_pedido);
                                            entregasRepositorio.entregaNotificada(ped.id_pedido);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE OS IDS DOS PEDIDOS VISUALIZADOS
                                    if (dados.retorno_pedidos_confirmado != null) {
                                        for (DadosEntrega ped : dados.retorno_pedidos_confirmado) {
                                            Log.i(TAG, "ID_CONFIRMADO:" + ped.id_pedido);
                                            entregasRepositorio.entregaConfirmada(ped.id_pedido);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                try {
                                    // RECEBE A LISTA DE PEDIDOS EM ABERTO
                                    if (dados.retorno_pedidos_abertos != null) {
                                        for (DadosEntrega ped : dados.retorno_pedidos_abertos) {
                                            Log.i(TAG, "ID_PED_ABERTO:" + ped.id_pedido);

                                            DadosEntrega dadEntrega = entregasRepositorio.getEntrega(ped.id_pedido);
                                            if (dadEntrega != null) {

                                                String la = "";
                                                String lo = "";
                                                String la2 = "";
                                                String lo2 = "";

                                                try {
                                                    la = String.valueOf(dadEntrega.coord_latitude).substring(0, 6);
                                                    lo = String.valueOf(dadEntrega.coord_longitude).substring(0, 6);
                                                    la2 = String.valueOf(ped.coord_latitude).substring(0, 6);
                                                    lo2 = String.valueOf(ped.coord_longitude).substring(0, 6);
                                                } catch (Exception ignored) {

                                                }
                                                String t1 = dadEntrega.id_pedido +
                                                        dadEntrega.telefone_pedido +
                                                        dadEntrega.status +
                                                        dadEntrega.troco_para +
                                                        dadEntrega.valor +
                                                        dadEntrega.endereco +
                                                        dadEntrega.observacao +
                                                        dadEntrega.brindes +
                                                        dadEntrega.forma_pagamento +
                                                        dadEntrega.produtos +
                                                        dadEntrega.id_cliente +
                                                        dadEntrega.cliente +
                                                        dadEntrega.apelido +
                                                        dadEntrega.localidade +
                                                        dadEntrega.numero +
                                                        dadEntrega.complemento +
                                                        dadEntrega.ponto_referencia +
                                                        la +
                                                        lo +
                                                        dadEntrega.ativar_btn_ligar;
                                                String t2 = ped.id_pedido +
                                                        ped.telefone_pedido +
                                                        ped.status +
                                                        ped.troco_para +
                                                        ped.valor +
                                                        ped.endereco +
                                                        ped.observacao +
                                                        ped.brindes +
                                                        ped.forma_pagamento +
                                                        ped.produtos +
                                                        ped.id_cliente +
                                                        ped.cliente +
                                                        ped.apelido +
                                                        ped.localidade +
                                                        ped.numero +
                                                        ped.complemento +
                                                        ped.ponto_referencia +
                                                        la2 +
                                                        lo2 +
                                                        ped.ativar_btn_ligar;


                                                String s1 = new ClassAuxiliar().md5(t1);
                                                String s2 = new ClassAuxiliar().md5(t2);
                                                Log.i(TAG, "COMPARAR:\n" + s1 + "\n" + s2);
                                                //Log.i(TAG, "COMPARAR:\n" + t1 +"\n" + t2);

                                                if (!t1.equalsIgnoreCase(t2)) {

                                                    Log.i(TAG, "ID_PED_EDITADO:" + ped.id_pedido);

                                                    // EXCLUI A ENTREGA
                                                    entregasRepositorio.excluir(ped.id_pedido);

                                                    // CRIA A NOVA ENTREGA PARA SALVAR NO BANCO DE DADOS
                                                    DadosEntrega dadosEntrega = new DadosEntrega();
                                                    dadosEntrega.id_pedido = ped.id_pedido;
                                                    dadosEntrega.hora_recebimento = ped.hora_recebimento;
                                                    dadosEntrega.nome_atendente = ped.nome_atendente;
                                                    dadosEntrega.telefone_pedido = ped.telefone_pedido;
                                                    dadosEntrega.status = ped.status;
                                                    dadosEntrega.troco_para = ped.troco_para;
                                                    dadosEntrega.valor = ped.valor;
                                                    dadosEntrega.id_cliente = ped.id_cliente;
                                                    dadosEntrega.cliente = ped.cliente;
                                                    dadosEntrega.apelido = ped.apelido;
                                                    dadosEntrega.endereco = ped.endereco;
                                                    dadosEntrega.localidade = ped.localidade;
                                                    dadosEntrega.numero = ped.numero;
                                                    dadosEntrega.complemento = ped.complemento;
                                                    dadosEntrega.ponto_referencia = ped.ponto_referencia;
                                                    dadosEntrega.coord_latitude = ped.coord_latitude;
                                                    dadosEntrega.coord_longitude = ped.coord_longitude;
                                                    dadosEntrega.produtos = ped.produtos;
                                                    dadosEntrega.brindes = ped.brindes;
                                                    dadosEntrega.observacao = ped.observacao;
                                                    dadosEntrega.forma_pagamento = ped.forma_pagamento;
                                                    dadosEntrega.ativar_btn_ligar = ped.ativar_btn_ligar;
                                                    entregasRepositorio.inserir(dadosEntrega);

                                                    TemPedido = true;

                                                    int index = 0;
                                                    for (String pId : IdPedMudEntregador) {
                                                        if (pId.equals(ped.id_pedido))
                                                            IdPedMudEntregador.remove(index);

                                                        index++;
                                                    }
                                                }
                                            }

                                            if (ped.status.equalsIgnoreCase("P") && !ped.id_pedido.equalsIgnoreCase("0")) {

                                                //VERIFICA SE A ENTREGA JÁ FOI GRAVADA NO BANCO DE DADOS OU O PEDIDO FOI RETRNADO PARA O MESMO ENTREGADOR
                                                //Log.e("Pedido", dados.toString());
                                                if (entregasRepositorio.verificarPedidoGravado(ped.id_pedido) == null || entregasRepositorio.verificarStatusPedidoGravado(ped.id_pedido).equalsIgnoreCase("EM")) {
                                                    // EXCLUI A ENTREGA
                                                    entregasRepositorio.excluir(ped.id_pedido);

                                                    // CRIA A NOVA ENTREGA PARA SALVAR NO BANCO DE DADOS
                                                    DadosEntrega dadosEntrega = new DadosEntrega();
                                                    dadosEntrega.id_pedido = ped.id_pedido;
                                                    dadosEntrega.hora_recebimento = ped.hora_recebimento;
                                                    dadosEntrega.nome_atendente = ped.nome_atendente;
                                                    dadosEntrega.telefone_pedido = ped.telefone_pedido;
                                                    dadosEntrega.status = ped.status;
                                                    dadosEntrega.troco_para = ped.troco_para;
                                                    dadosEntrega.valor = ped.valor;
                                                    dadosEntrega.id_cliente = ped.id_cliente;
                                                    dadosEntrega.cliente = ped.cliente;
                                                    dadosEntrega.apelido = ped.apelido;
                                                    dadosEntrega.endereco = ped.endereco;
                                                    dadosEntrega.localidade = ped.localidade;
                                                    dadosEntrega.numero = ped.numero;
                                                    dadosEntrega.complemento = ped.complemento;
                                                    dadosEntrega.ponto_referencia = ped.ponto_referencia;
                                                    dadosEntrega.coord_latitude = ped.coord_latitude;
                                                    dadosEntrega.coord_longitude = ped.coord_longitude;
                                                    dadosEntrega.produtos = ped.produtos;
                                                    dadosEntrega.brindes = ped.brindes;
                                                    dadosEntrega.observacao = ped.observacao;
                                                    dadosEntrega.forma_pagamento = ped.forma_pagamento;
                                                    dadosEntrega.ativar_btn_ligar = ped.ativar_btn_ligar;
                                                    entregasRepositorio.inserir(dadosEntrega);

                                                    //
                                                    TemPedido = true;

                                                    int index = 0;
                                                    for (String pId : IdPedMudEntregador) {
                                                        if (pId.equals(ped.id_pedido))
                                                            IdPedMudEntregador.remove(index);

                                                        index++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception: " + e.getMessage());
                                }

                                if (TemPedido) {
                                    //
                                    Intent i = new Intent();
                                    i.setClassName("br.com.zenitech.zcallmobile", "br.com.zenitech.zcallmobile.NovaEntrega");
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(i);
                                }
                            } else {
                                _inserirPosOffLine();
                            }
                        } else {
                            _inserirPosOffLine();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JSonZCall> call, @NonNull Throwable t) {
                        Log.e(TAG, "Throwable: " + t.getMessage());
                        _inserirPosOffLine();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                _inserirPosOffLine();
            }
        } else {
            Log.e(TAG, "Sem internet");
            _inserirPosOffLine();
        }
    }

    private void _inserirPosOffLine() {
        Log.i(TAG, "ERROR");
        //criarConexao();
        posicoesRepositorio.inserir(String.valueOf(lat), String.valueOf(lon));
    }

    private void temporizador() {
        new Handler().postDelayed(() -> {
            if (prefs.getString("ponto", "").equalsIgnoreCase("ok")) {
                enviarDados();
            }

            //CHAMA O TEMPORIZADOR NOVAMENTE
            temporizador();
        }, 5000);

    }

    private void criarConexao() {
        try {
            if (dataBaseOpenHelper == null) dataBaseOpenHelper = new DataBaseOpenHelper(context);
            if (conexao == null) conexao = dataBaseOpenHelper.getWritableDatabase();
            if (entregasRepositorio == null) entregasRepositorio = new EntregasRepositorio(conexao);
            if (posicoesRepositorio == null)
                posicoesRepositorio = new PosicoesRepositorio(conexao, new ClassAuxiliar());
            if (sistematicaRepositorio == null)
                sistematicaRepositorio = new SistematicaRepositorio(conexao, new ClassAuxiliar());
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }

    public void opcoesContato(Activity context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("O ZCall Mobile, coleta dados de localização a fim de informar para central, qual entregador está mais próximo do endereço do pedido.");
            builder.setNeutralButton("Sair", (arg0, arg1) -> {
            });
            builder.setPositiveButton("Ok", (dialogInterface, i) -> {
                dialogInterface.cancel();
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            });

            builder.create();
            builder.show();
        }
    }
}
