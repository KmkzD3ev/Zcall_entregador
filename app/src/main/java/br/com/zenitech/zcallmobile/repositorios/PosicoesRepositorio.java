package br.com.zenitech.zcallmobile.repositorios;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.zcallmobile.ClassAuxiliar;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.domais.DadosPosicoes;

/**
 * Created by Kleilson Sousa on 26/11/2020.
 */

public class PosicoesRepositorio {

    private static String TB_POSICOES = "posicoes";
    private SQLiteDatabase conexao;
    private ClassAuxiliar cAux;

    public PosicoesRepositorio(SQLiteDatabase conexao, ClassAuxiliar cAux) {
        this.conexao = conexao;
        this.cAux = cAux;
    }

    // INSERIR POSIÇÕES OFFLINE
    public void inserir(String lat, String lon) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("latitude", lat);
        contentValues.put("longitude", lon);
        contentValues.put("data_time", cAux.inserirDataAtual() + " " + cAux.horaAtual());

        Log.i("KLEILSON", contentValues.toString());
        //
        conexao.insertOrThrow(TB_POSICOES, null, contentValues);
    }

    public List<DadosPosicoes> ListaPosicoes() {
        //
        List<DadosPosicoes> posicoes = new ArrayList<>();

        //
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append("SELECT * ");
        sql.append("FROM ").append(TB_POSICOES).append(" ");
        sql.append("ORDER BY id DESC");

        Log.i("KLEILSON", sql.toString());

        //
        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        //
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                //
                DadosPosicoes dp = new DadosPosicoes();
                dp.id = resultado.getString(resultado.getColumnIndexOrThrow("id"));
                dp.latitude = resultado.getString(resultado.getColumnIndexOrThrow("latitude"));
                dp.longitude = resultado.getString(resultado.getColumnIndexOrThrow("longitude"));
                dp.data_time = resultado.getString(resultado.getColumnIndexOrThrow("data_time"));

                Log.i("KLEILSON", dp.toString());
                posicoes.add(dp);

            } while (resultado.moveToNext());
        }

        return posicoes;
    }

    public void excluir(String id) {
        //
        String[] parametros = new String[1];
        parametros[0] = id;

        //
        conexao.delete(TB_POSICOES, "id = ? ", parametros);
    }
}
