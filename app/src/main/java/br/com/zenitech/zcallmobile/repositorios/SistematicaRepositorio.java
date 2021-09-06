package br.com.zenitech.zcallmobile.repositorios;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.zcallmobile.ClassAuxiliar;
import br.com.zenitech.zcallmobile.domais.DadosVendasSistematica;

/**
 * Created by Kleilson Sousa on 26/11/2020.
 */

public class SistematicaRepositorio {

    private final SQLiteDatabase conexao;
    private ClassAuxiliar aux;

    public SistematicaRepositorio(SQLiteDatabase conexao, ClassAuxiliar aux) {
        this.conexao = conexao;
        this.aux = aux;
    }

    // CONFIGURAÇÕES SISTEMÁTICA - INSERIR FORMAS PAGAMENTO
    public void inserirFormasPagamento(String id_forma_pagamento, String forma_pagamento) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_forma_pagamento", id_forma_pagamento);
        contentValues.put("forma_pagamento", forma_pagamento);

        //
        conexao.insertOrThrow("formas_pagamento", null, contentValues);
    }

    // CONFIGURAÇÕES SISTEMÁTICA - INSERIR PRODUTOS
    public void inserirProdutos(String id_produto, String produto) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_produto", id_produto);
        contentValues.put("produto", produto);

        //
        conexao.insertOrThrow("produtos", null, contentValues);
    }

    // CONFIGURAÇÕES SISTEMÁTICA - EXCLUIR FORMAS PAGAMENTO E PRODUTOS
    public void excluir() {
        //
        conexao.delete("formas_pagamento", null, null);
        //
        conexao.delete("produtos", null, null);
    }

    // ******************** VENDAS SISTEMÁTICA *******************
    public ArrayList<String> getFormasPagamento() {
        ArrayList<String> list = new ArrayList<>();
        list.add("FORMA DE PAGAMENTO");
        //
        String selectQuery = "SELECT * FROM formas_pagamento";

        Cursor cursor = conexao.rawQuery(selectQuery, null);

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(cursor.getColumnIndex("forma_pagamento")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getProdutos() {
        ArrayList<String> list = new ArrayList<>();
        list.add("PRODUTO");
        //
        String selectQuery = "SELECT * FROM produtos";

        Cursor cursor = conexao.rawQuery(selectQuery, null);

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(cursor.getColumnIndex("produto")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ** Enviar dados
    public String IdProduto(String produto) {
        String query = "SELECT id_produto FROM produtos WHERE produto = '" + produto + "'";
        Log.e("SQL", "IdProduto - " + query);

        Cursor cursor = conexao.rawQuery(query, null);
        String str = "";

        if (cursor.moveToFirst()) {
            do {
                str = cursor.getString(cursor.getColumnIndex("id_produto"));
            } while (cursor.moveToNext());
        }

        return str;
    }

    // ** Enviar dados
    public String IdFormaPagamento(String fpg) {

        String query = "SELECT id_forma_pagamento  FROM formas_pagamento WHERE forma_pagamento = '" + fpg + "' LIMIT 1";
        //Log.i("Sistematica", query);
        Cursor cursor = conexao.rawQuery(query, null);
        StringBuilder str = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                str.append(cursor.getString(cursor.getColumnIndex("id_forma_pagamento")));
            } while (cursor.moveToNext());
        }

        return str.toString();
    }

    public void salvarVendaSistematica(String id_forma_pagamento, String id_produto, String quantidade, String valor) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("data", aux.inserirDataAtual());
        contentValues.put("hora_recebimento", aux.horaAtual());
        contentValues.put("id_forma_pagamento", id_forma_pagamento);
        contentValues.put("id_produto", id_produto);
        contentValues.put("quantidade", quantidade);
        contentValues.put("valor", valor);

        //
        conexao.insertOrThrow("vendas_sistematica", null, contentValues);
    }

    public List<DadosVendasSistematica> getVendasSistematica() {
        //
        List<DadosVendasSistematica> vendasSistematicas = new ArrayList<>();

        //
        String sql = "SELECT vsi.id, vsi.data, vsi.hora_recebimento, vsi.id_forma_pagamento, vsi.id_produto, vsi.quantidade, vsi.valor FROM vendas_sistematica vsi";

        Log.i("KLEILSON", sql);

        //
        Cursor resultado = conexao.rawQuery(sql, null);

        //
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();
            do {
                //
                DadosVendasSistematica dados = new DadosVendasSistematica();
                dados.id = resultado.getString(resultado.getColumnIndexOrThrow("id"));
                dados.data = resultado.getString(resultado.getColumnIndexOrThrow("data"));
                dados.hora_recebimento = resultado.getString(resultado.getColumnIndexOrThrow("hora_recebimento"));
                dados.id_forma_pagamento = resultado.getString(resultado.getColumnIndexOrThrow("id_forma_pagamento"));
                dados.id_produto = resultado.getString(resultado.getColumnIndexOrThrow("id_produto"));
                dados.quantidade = resultado.getString(resultado.getColumnIndexOrThrow("quantidade"));
                dados.valor = resultado.getString(resultado.getColumnIndexOrThrow("valor"));

                //Log.i("KLEILSON", dados.toString());
                vendasSistematicas.add(dados);

            } while (resultado.moveToNext());
        }
        resultado.close();
        return vendasSistematicas;
    }

    //
    public int deleteVendasSistematica(String id) {


        int i = conexao.delete(
                "vendas_sistematica",
                "id = ?",
                new String[]{id}
        );

        return i;
    }
}
