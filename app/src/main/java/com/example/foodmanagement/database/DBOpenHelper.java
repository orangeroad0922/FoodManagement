package com.example.foodmanagement.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "FoodDB.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FoodExpirationContract.FoodExpiration.TABLE_NAME + " (" +
                    FoodExpirationContract.FoodExpiration._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FoodExpirationContract.FoodExpiration.COLUMN_NAME_FOODNAME + " TEXT," +
                    FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FoodExpirationContract.FoodExpiration.TABLE_NAME;


    public DBOpenHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void saveFood( SQLiteDatabase db, String title, Date expiration){
        ContentValues values = new ContentValues();
        values.put( FoodExpirationContract.FoodExpiration.COLUMN_NAME_FOODNAME, title );
        values.put(FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION, expiration.getTime());

        db.insert(FoodExpirationContract.FoodExpiration.TABLE_NAME, null, values);
    }
}