package br.com.zenitech.zcallmobile.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kleilson Sousa on 4/6/18.
 */

public class DataBaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "zcall"; //

    public DataBaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScriptDLL.CreateTableEntregas());
        db.execSQL(ScriptDLL.CreateTablePosicoes());
        db.execSQL(ScriptDLL.CreateTableVendasSistematica());
        db.execSQL(ScriptDLL.CreateTableFormasPagamento());
        db.execSQL(ScriptDLL.CreateTableProdutos());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS entregas");
        db.execSQL("DROP TABLE IF EXISTS posicoes");
        db.execSQL("DROP TABLE IF EXISTS vendas_sistematica");
        db.execSQL("DROP TABLE IF EXISTS formas_pagamento");
        db.execSQL("DROP TABLE IF EXISTS produtos");
        onCreate(db);

        //onUpgrade(db, oldVersion, newVersion);
    }
}
