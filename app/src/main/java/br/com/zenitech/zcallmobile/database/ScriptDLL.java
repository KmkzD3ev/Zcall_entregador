package br.com.zenitech.zcallmobile.database;

import android.util.Log;

/**
 * Created by kma_s on 6/6/18.
 */

public class ScriptDLL {

    public static String CreateTableEntregas() {
        StringBuilder sql;
        sql = new StringBuilder();

        sql.append("CREATE TABLE entregas (");
        sql.append("    id                  INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,");
        sql.append("    id_pedido           VARCHAR (11), ");
        sql.append("    hora_recebimento    VARCHAR (15), ");
        sql.append("    nome_atendente      VARCHAR (50), ");
        sql.append("    telefone_pedido     VARCHAR (20), ");
        sql.append("    status              VARCHAR (20), ");
        sql.append("    troco_para          VARCHAR (11), ");
        sql.append("    valor               VARCHAR (11), ");
        sql.append("    id_cliente          VARCHAR (11), ");
        sql.append("    cliente             VARCHAR (100), ");
        sql.append("    apelido             VARCHAR (70), ");
        sql.append("    endereco            VARCHAR (300), ");
        sql.append("    localidade          VARCHAR (100), ");
        sql.append("    numero              VARCHAR (100), ");
        sql.append("    complemento         VARCHAR (100), ");
        sql.append("    ponto_referencia    VARCHAR (150), ");
        sql.append("    coord_latitude      VARCHAR (50), ");
        sql.append("    coord_longitude     VARCHAR (50), ");
        sql.append("    produtos            VARCHAR (2000), ");
        sql.append("    brindes             VARCHAR (200), ");
        sql.append("    observacao          VARCHAR (500), ");
        sql.append("    forma_pagamento     VARCHAR (50), ");
        sql.append("    ativar_btn_ligar    VARCHAR (1), ");
        sql.append("    visualizada         VARCHAR (1), ");
        sql.append("    notificada          VARCHAR (1), ");
        sql.append("    finalizada          VARCHAR (1), ");
        sql.append("    confirmado          VARCHAR (1)");
        sql.append(")");

        Log.i("KLEILSON", sql.toString());

        return sql.toString();
    }

    public static String CreateTablePosicoes() {
        StringBuilder sql;
        sql = new StringBuilder();

        sql.append("CREATE TABLE posicoes (");
        sql.append("    id                  INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,");
        sql.append("    latitude            VARCHAR (50), ");
        sql.append("    longitude           VARCHAR (50), ");
        sql.append("    data_time           VARCHAR (50)");
        sql.append(")");

        Log.i("KLEILSON", sql.toString());

        return sql.toString();
    }

    public static String CreateTableVendasSistematica() {
        StringBuilder sql;
        sql = new StringBuilder();

        sql.append("CREATE TABLE vendas_sistematica (");
        sql.append("    id                  INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,");
        sql.append("    id_unidade          INTEGER, ");
        sql.append("    data                TEXT, ");
        sql.append("    hora_recebimento    TEXT, ");
        sql.append("    id_entregador       INTEGER, ");
        sql.append("    status              TEXT, ");
        sql.append("    id_forma_pagamento  INTEGER, ");
        sql.append("    id_produto          INTEGER, ");
        sql.append("    quantidade          TEXT, ");
        sql.append("    valor               TEXT");
        sql.append(")");

        Log.i("KLEILSON", sql.toString());

        return sql.toString();
    }

    public static String CreateTableFormasPagamento() {
        StringBuilder sql;
        sql = new StringBuilder();

        sql.append("CREATE TABLE formas_pagamento (");
        sql.append("    id                      INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,");
        sql.append("    id_forma_pagamento      INTEGER, ");
        sql.append("    forma_pagamento         VARCHAR (50)");
        sql.append(")");

        Log.i("KLEILSON", sql.toString());

        return sql.toString();
    }

    public static String CreateTableProdutos() {
        StringBuilder sql;
        sql = new StringBuilder();

        sql.append("CREATE TABLE produtos (");
        sql.append("    id                  INTEGER         PRIMARY KEY AUTOINCREMENT   NOT NULL,");
        sql.append("    id_produto          INTEGER, ");
        sql.append("    produto             VARCHAR (50)");
        sql.append(")");

        Log.i("KLEILSON", sql.toString());

        return sql.toString();
    }

    // UPDATE VERSÃO 1 - ADD COLUMN arquivado
    public static String updateEntregas1() {

        StringBuilder sql;
        sql = new StringBuilder();
        sql.append("ALTER TABLE entregas ADD COLUMN visualizada VARCHAR (1), ");
        sql.append("ADD COLUMN ok VARCHAR (1)");

        Log.i("KLEILSON", sql.toString());
        return sql.toString();
    }
}
