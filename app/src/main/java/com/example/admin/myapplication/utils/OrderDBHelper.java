package com.example.admin.myapplication.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by GogHox on 2017/12/10.
 */

public class OrderDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "order.db";
    private static final int VERSION = 1;
    public static final String TABLE_ORDER = "my_order";

    public OrderDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_ORDER + "(id integer primary key autoincrement, order_id varchar(255), token varchar(255));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists " + TABLE_ORDER + ";";
        db.execSQL(sql);
        onCreate(db);
    }

    public void insertOrder(String orderID, String token) {
        String sql = "insert into " + TABLE_ORDER + " (order_id, token) values('" + orderID + "', '" + token + "')";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public Cursor queryOrder() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"id", "order_id", "token"};
        Cursor cursor = db.query(TABLE_ORDER, columns, null, null, null, null, null);
        return cursor;
    }

    public void deleteOrder(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "delete from " + TABLE_ORDER + " where id = " + id;
        db.execSQL(sql);
        db.close();
    }
}












