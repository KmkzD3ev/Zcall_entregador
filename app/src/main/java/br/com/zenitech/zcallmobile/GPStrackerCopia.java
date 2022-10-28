package br.com.zenitech.zcallmobile;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

import java.util.List;

import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.domais.DadosGerenciarInfor;
import br.com.zenitech.zcallmobile.domais.DadosPosicoes;
import br.com.zenitech.zcallmobile.domais.DadosVendasSistematica;
import br.com.zenitech.zcallmobile.domais.PosicoesDomains;
import br.com.zenitech.zcallmobile.interfaces.IPosicoes;
import br.com.zenitech.zcallmobile.repositorios.PosicoesRepositorio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPStrackerCopia {

    //
    SQLiteDatabase conexao;
    DataBaseOpenHelper dataBaseOpenHelper;
    PosicoesRepositorio posicoesRepositorio;
    SharedPreferences prefs;
    Context context;
    //
    private static final String TAG = "GPStracker";
    private double lat = 0;
    private double lon = 0;

    List<DadosPosicoes> dados;

    List<DadosEntrega> pedidos;
    List<DadosPosicoes> posicoes;
    List<DadosVendasSistematica> sistematicas;

    //
    public static GPStrackerCopia instance;
    public static boolean TemPedido = false;
    private GPStrackerCopia(Context c) {
        context = c.getApplicationContext();
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);

        criarConexao();
        temporizador();
    }

    public static GPStrackerCopia getInstance(Context context) {
        if (instance == null) {
            instance = new GPStrackerCopia(context);
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

    private void _salvarPosicao() {
        // VERIFICA SE TEM INTERNET
        if (new VerificarOnline().isOnline(context)) {
            try {
                //
                prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);

                //
                final IPosicoes iPosicoes = IPosicoes.retrofit.create(IPosicoes.class);
                final Call<DadosGerenciarInfor> call = iPosicoes.PosicoesComResultado(
                        prefs.getString("id_empresa", ""),
                        prefs.getString("telefone", ""),
                        "SetGetInfo", //salvar
                        lat,
                        lon
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<DadosGerenciarInfor> call, @NonNull Response<DadosGerenciarInfor> response) {
                        if (response.isSuccessful()) {
                            DadosGerenciarInfor dados = response.body();
                            if (dados != null) {

                                try {
                                    Log.i(TAG, dados.entrega);

                                    if (dados.entrega.equalsIgnoreCase("1")) {
                                        //TemPedido = true;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            } else {
                                _inserirPosOffLine();
                            }
                        } else {
                            _inserirPosOffLine();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DadosGerenciarInfor> call, @NonNull Throwable t) {
                        _inserirPosOffLine();
                    }
                });
            } catch (Exception ignored) {
                _inserirPosOffLine();
            }
        } else {
            _inserirPosOffLine();
        }
    }

    private void _inserirPosOffLine() {
        Log.i(TAG, "ERROR");
        //criarConexao();
        posicoesRepositorio.inserir(String.valueOf(lat), String.valueOf(lon));
    }

    private void _salvarPosicaoOffLine(String id, String lat, String lon, String dataTime) {
        //
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        // VERIFICA SE TEM INTERNET
        if (new VerificarOnline().isOnline(context)) {
            try {
                //
                final IPosicoes iPosicoes = IPosicoes.retrofit.create(IPosicoes.class);
                final Call<PosicoesDomains> call = iPosicoes.PosicoesOffLine(
                        prefs.getString("id_empresa", ""),
                        prefs.getString("telefone", ""),
                        "salvarPosOff",
                        lat,
                        lon,
                        dataTime
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<PosicoesDomains> call, @NonNull Response<PosicoesDomains> response) {
                        if (response.isSuccessful()) {
                            PosicoesDomains dados = response.body();
                            if (dados != null) {
                                try {
                                    if (!dados.getStatus().equalsIgnoreCase("erro")) {
                                        //Toast.makeText(context, dados.getStatus(), Toast.LENGTH_SHORT).show();
                                        //VERIFICA SE A ENTREGA JÁ FOI GRAVADA NO BANCO DE DADOS
                                        //Log.i(TAG, dados.getStatus());
                                    } else {
                                        posicoesRepositorio.excluir(id);
                                    }
                                } catch (Exception ignored) {

                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PosicoesDomains> call, @NonNull Throwable t) {
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    private void temporizador() {
        new Handler().postDelayed(() -> {
            if (prefs.getString("localizar", "0").equalsIgnoreCase("1") &&
                    prefs.getString("ponto", "").equalsIgnoreCase("ok")
            ) {
                try {
                    if (lat != 0.0 && lon != 0.0) {
                        //VERIFICA SE O APARELHO ESTÁ CONECTADO A INTERNET
                        _salvarPosicao();
                    }

                    try {
                        dados = posicoesRepositorio.ListaPosicoes();
                        int sizePos = Math.min(dados.size(), 10);
                        for (int i = 0; sizePos > i; i++) {
                            _salvarPosicaoOffLine(dados.get(i).id, dados.get(i).latitude, dados.get(i).longitude, dados.get(i).data_time);
                        }
                    } catch (Exception ignored) {

                    }
                } catch (Exception ignored) {

                }
            }

            //CHAMA O TEMPORIZADOR NOVAMENTE
            temporizador();
        }, 10000);

    }

    private void criarConexao() {
        try {
            dataBaseOpenHelper = new DataBaseOpenHelper(context);
            conexao = dataBaseOpenHelper.getWritableDatabase();
            posicoesRepositorio = new PosicoesRepositorio(conexao, new ClassAuxiliar());
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
                    }
            );

            builder.create();
            builder.show();
        }
    }
}
