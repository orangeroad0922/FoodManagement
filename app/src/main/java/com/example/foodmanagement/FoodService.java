package com.example.foodmanagement;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foodmanagement.database.DBOpenHelper;
import com.example.foodmanagement.database.FoodExpirationContract;
import com.example.foodmanagement.model.FoodData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FoodService {

    /**
     * 食品一覧をDBから取得し返却する
     *
     * @return
     */
    public List<FoodData> getFoodList( Context context ) {

        DBOpenHelper dbOpenHelper = new DBOpenHelper( context );
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String[] columns = {
                FoodExpirationContract.FoodExpiration._ID,
                FoodExpirationContract.FoodExpiration.COLUMN_NAME_FOODNAME,
                FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION
        };
        String sortOrder = FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION + " DESC";
        Cursor cursor = db.query( FoodExpirationContract.FoodExpiration.TABLE_NAME, columns,
                null, null, null, null, sortOrder );

        List<FoodData> foodList = new ArrayList<>();
        while (cursor.moveToNext()) {
            FoodData foodData = new FoodData();
            foodData.setId( cursor.getInt( cursor.getColumnIndexOrThrow( FoodExpirationContract.FoodExpiration.COLUMN_NAME_ID ) ) );
            foodData.setFoodName( cursor.getString( cursor.getColumnIndexOrThrow( FoodExpirationContract.FoodExpiration.COLUMN_NAME_FOODNAME ) ) );
            foodData.setExpiration(
                    new Date( cursor.getLong(
                            cursor.getColumnIndexOrThrow( FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION ) ) * 1000 ) );
            foodList.add( foodData );
        }

        cursor.close();
        return foodList;
    }
}
