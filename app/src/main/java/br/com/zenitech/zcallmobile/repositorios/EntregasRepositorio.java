package br.com.zenitech.zcallmobile.repositorios;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.zenitech.zcallmobile.domais.DadosEntrega;

/**
 * Created by Kleilson Sousa on 6/6/18.
 */

public class EntregasRepositorio {

    private static String TB_ENTREGAS = "entregas";
    private SQLiteDatabase conexao;

    public EntregasRepositorio(SQLiteDatabase conexao) {
        this.conexao = conexao;
    }

    public void inserir(DadosEntrega entrega) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("id_pedido", entrega.id_pedido);
        contentValues.put("hora_recebimento", entrega.hora_recebimento);
        contentValues.put("nome_atendente", entrega.nome_atendente);
        contentValues.put("telefone_pedido", entrega.telefone_pedido);
        contentValues.put("status", entrega.status);
        contentValues.put("troco_para", entrega.troco_para);
        contentValues.put("valor", entrega.valor);
        contentValues.put("id_cliente", entrega.id_cliente);
        contentValues.put("cliente", entrega.cliente);
        contentValues.put("apelido", entrega.apelido);
        contentValues.put("endereco", entrega.endereco);
        contentValues.put("localidade", entrega.localidade);
        contentValues.put("numero", entrega.numero);
        contentValues.put("complemento", entrega.complemento);
        contentValues.put("ponto_referencia", entrega.ponto_referencia);
        contentValues.put("coord_latitude", entrega.coord_latitude);
        contentValues.put("coord_longitude", entrega.coord_longitude);
        contentValues.put("produtos", entrega.produtos);
        contentValues.put("brindes", entrega.brindes);
        contentValues.put("observacao", entrega.observacao);
        contentValues.put("forma_pagamento", entrega.forma_pagamento);
        contentValues.put("ativar_btn_ligar", entrega.ativar_btn_ligar);
        contentValues.put("visualizada", "0");
        contentValues.put("notificada", "0");
        contentValues.put("finalizada", "0");
        contentValues.put("confirmado", "0");

        Log.i("KLEILSON", contentValues.toString());
        //
        conexao.insertOrThrow(TB_ENTREGAS, null, contentValues);
    }

    public List<DadosEntrega> ListaEntregas() {
        //
        List<DadosEntrega> jogos = new ArrayList<>();

        //
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT * ");
        sql.append(" FROM ").append(TB_ENTREGAS).append(" ");
        //sql.append(" WHERE finalizada != '1' ");
        //sql.append(" ORDER BY id_pedido DESC ");
        sql.append(" ORDER BY id_pedido DESC, finalizada ");// DESC
        Log.i("KLEILSON", sql.toString());

        //
        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                do {
                    //
                    DadosEntrega ms = new DadosEntrega();
                    ms.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
                    ms.hora_recebimento = resultado.getString(resultado.getColumnIndexOrThrow("hora_recebimento"));
                    ms.nome_atendente = resultado.getString(resultado.getColumnIndexOrThrow("nome_atendente"));
                    ms.telefone_pedido = resultado.getString(resultado.getColumnIndexOrThrow("telefone_pedido"));
                    ms.status = resultado.getString(resultado.getColumnIndexOrThrow("status"));
                    ms.troco_para = resultado.getString(resultado.getColumnIndexOrThrow("troco_para"));
                    ms.valor = resultado.getString(resultado.getColumnIndexOrThrow("valor"));
                    ms.id_cliente = resultado.getString(resultado.getColumnIndexOrThrow("id_cliente"));
                    ms.cliente = resultado.getString(resultado.getColumnIndexOrThrow("cliente"));
                    ms.apelido = resultado.getString(resultado.getColumnIndexOrThrow("apelido"));
                    ms.endereco = resultado.getString(resultado.getColumnIndexOrThrow("endereco"));
                    ms.localidade = resultado.getString(resultado.getColumnIndexOrThrow("localidade"));
                    ms.numero = resultado.getString(resultado.getColumnIndexOrThrow("numero"));
                    ms.complemento = resultado.getString(resultado.getColumnIndexOrThrow("complemento"));
                    ms.ponto_referencia = resultado.getString(resultado.getColumnIndexOrThrow("ponto_referencia"));
                    ms.coord_latitude = resultado.getDouble(resultado.getColumnIndexOrThrow("coord_latitude"));
                    ms.coord_longitude = resultado.getDouble(resultado.getColumnIndexOrThrow("coord_longitude"));
                    ms.produtos = resultado.getString(resultado.getColumnIndexOrThrow("produtos"));
                    ms.brindes = resultado.getString(resultado.getColumnIndexOrThrow("brindes"));
                    ms.observacao = resultado.getString(resultado.getColumnIndexOrThrow("observacao"));
                    ms.forma_pagamento = resultado.getString(resultado.getColumnIndexOrThrow("forma_pagamento"));
                    ms.ativar_btn_ligar = resultado.getString(resultado.getColumnIndexOrThrow("ativar_btn_ligar"));
                    ms.finalizada = resultado.getString(resultado.getColumnIndexOrThrow("finalizada"));
                    ms.confirmado = resultado.getString(resultado.getColumnIndexOrThrow("confirmado"));

                    Log.i("KLEILSON", ms.toString());
                    jogos.add(ms);

                } while (resultado.moveToNext());
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return jogos;
    }

    public void limparDados() {
        //
        conexao.delete(TB_ENTREGAS, null, null);
    }

    public void excluir(String id) {
        //
        String[] parametros = new String[1];
        parametros[0] = id;

        //
        conexao.delete(TB_ENTREGAS, "id_pedido = ? ", parametros);
    }

    public void alterar(String id, double latitude, double longitude) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("finalizada", "1");
        contentValues.put("coord_latitude", latitude);
        contentValues.put("coord_longitude", longitude);

        //
        String[] parametros = new String[1];
        parametros[0] = String.valueOf(id);

        //
        conexao.update(TB_ENTREGAS, contentValues, "id_pedido = ? ", parametros);
    }

    /*
    public String finalizarEntrega() {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM ").append(TB_ENTREGAS).append(" WHERE finalizada = '1' LIMIT 1");

        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        //
        if (resultado.getCount() > 0) {
            resultado.moveToFirst();

            return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
        }

        return null;
    }
    */

    public DadosEntrega finalizarEntrega() {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido, coord_latitude, coord_longitude FROM ").append(TB_ENTREGAS).append(" WHERE finalizada = '1' LIMIT 1");

        Cursor resultado = conexao.rawQuery(sql.toString(), null);
        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
                dadosEntrega.coord_latitude = resultado.getDouble(resultado.getColumnIndexOrThrow("coord_latitude"));
                dadosEntrega.coord_longitude = resultado.getDouble(resultado.getColumnIndexOrThrow("coord_longitude"));
                return dadosEntrega;
            }
        } catch (Exception e) {
            Log.e("Sql", e.getMessage());
        }
        resultado.close();
        return null;
    }

    public List<DadosEntrega> entregasFinalizadasOperador() {
        //
        List<DadosEntrega> dadosEntrega = new ArrayList<>();
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM entregas");// WHERE status = 'P'

        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();

                //return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
                do {
                    //
                    DadosEntrega ms = new DadosEntrega();
                    ms.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));

                    Log.i("KLEILSON", ms.toString());
                    dadosEntrega.add(ms);

                } while (resultado.moveToNext());
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return dadosEntrega;
    }

    public DadosEntrega EntregaMudouEntregador() {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM ").append(TB_ENTREGAS).append(" LIMIT 1");

        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
                return dadosEntrega;
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return null;
    }

    public List<DadosEntrega> ListaEntregaMudouEntregador() {
        //
        List<DadosEntrega> dadosEntrega = new ArrayList<>();

        //
        String Sql = "SELECT id_pedido FROM entregas WHERE status = 'P'";
        /*StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM ").append(TB_ENTREGAS);// DESC*/
        Log.i("LEMudouEntregador", Sql);

        //
        Cursor resultado = conexao.rawQuery(Sql, null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                do {
                    //
                    DadosEntrega ms = new DadosEntrega();
                    ms.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));

                    Log.i("LEMudouEntregador", ms.toString());
                    dadosEntrega.add(ms);

                } while (resultado.moveToNext());
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return dadosEntrega;
    }

    public String entregasVisualizadas() {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();

        //
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM ").append(TB_ENTREGAS).append(" WHERE visualizada = '1' AND ok = '0' LIMIT 1");

        //
        Cursor resultado = conexao.rawQuery(sql.toString(), null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();

                return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return null;
    }

    // INFORMA QUE A ENTREGA CHEGOU ATÉ O APP DO ENTREGADOR COM SUCESSO
    public void entregaNotificada(String id_pedido) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("notificada", "1");

        //
        String[] parametros = new String[1];
        parametros[0] = String.valueOf(id_pedido);

        //
        conexao.update(TB_ENTREGAS, contentValues, "id_pedido = ? ", parametros);
    }

    // INFORMA QUE A ENTREGA CHEGOU ATÉ O APP DO ENTREGADOR COM SUCESSO
    public void entregaConfirmada(String id_pedido) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("confirmado", "1");

        //
        String[] parametros = new String[1];
        parametros[0] = String.valueOf(id_pedido);

        //
        conexao.update(TB_ENTREGAS, contentValues, "id_pedido = ? ", parametros);
    }

    public String verificarPedidoGravado(String id_pedido) {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();

        //
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append(" SELECT id_pedido FROM ").
                append(TB_ENTREGAS).append(" WHERE id_pedido = '").
                append(id_pedido).append("' LIMIT 1");
        //
        Cursor resultado = conexao.rawQuery(sql.toString(), null);
        try {
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return null;
    }

    public String verificarStatusPedidoGravado(String id_pedido) {
        //
        DadosEntrega dadosEntrega = new DadosEntrega();

        //
        String sql;
        sql = "SELECT status FROM entregas WHERE id_pedido = '" + id_pedido + "' LIMIT 1";
        //
        Cursor resultado = conexao.rawQuery(sql, null);
        try {
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();
                return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("status"));
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }
        resultado.close();
        return null;
    }

    public List<DadosEntrega> ListentregasSemNotificacao() {
        //
        List<DadosEntrega> dadosEntrega = new ArrayList<>();

        //
        String sql = "SELECT id_pedido FROM entregas WHERE notificada = '0' AND status != 'EM'";

        //
        Cursor resultado = conexao.rawQuery(sql, null);

        try {
            //
            if (resultado.getCount() > 0) {
                resultado.moveToFirst();

                //return dadosEntrega.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));
                do {
                    //
                    DadosEntrega ms = new DadosEntrega();
                    ms.id_pedido = resultado.getString(resultado.getColumnIndexOrThrow("id_pedido"));

                    Log.i("KLEILSON", ms.toString());
                    dadosEntrega.add(ms);

                } while (resultado.moveToNext());
            }
        } catch (Exception e) {
            Log.e("EntregasRepositorio", e.getMessage());
        }

        resultado.close();
        return dadosEntrega;
    }

    // INFORMA QUE A ENTREGA CHEGOU ATÉ O APP DO ENTREGADOR COM SUCESSO
    public void _entregasFinalizadasOperador(String id_pedido, String status, String nome_atendente) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);
        contentValues.put("nome_atendente", nome_atendente);
        Log.i("KLE", status);

        //
        String[] parametros = new String[1];
        parametros[0] = String.valueOf(id_pedido);

        //
        conexao.update(TB_ENTREGAS, contentValues, "id_pedido = ? ", parametros);
    }

    // INFORMA QUE A ENTREGA CHEGOU ATÉ O APP DO ENTREGADOR COM SUCESSO
    public void _entregasOperadorMudouEntregador(String id_pedido, String status, String nome_atendente) {
        //
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);
        contentValues.put("nome_atendente", nome_atendente);
        Log.i("KLE", status);

        //
        String[] parametros = new String[1];
        parametros[0] = String.valueOf(id_pedido);

        //
        conexao.update(TB_ENTREGAS, contentValues, "id_pedido = ? ", parametros);
    }
}
