package com.example.foodmanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.foodmanagement.adapter.foodListRecycleViewAdapter;
import com.example.foodmanagement.database.DBOpenHelper;
import com.example.foodmanagement.database.FoodExpirationContract;
import com.example.foodmanagement.model.FoodData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DBOpenHelper dbOpenHelper;
    private EditText expiration;
    private EditText foodName;
    private List<FoodData> foodList;
    private foodListRecycleViewAdapter adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // DBから一覧を取得
        foodList = getFoodList();

        // 一覧ビューの生成
        RecyclerView rv = findViewById( R.id.food_list_view );
        adapter = new foodListRecycleViewAdapter( foodList );

        LinearLayoutManager llm = new LinearLayoutManager( this );
        rv.setHasFixedSize( true );
        rv.setLayoutManager( llm );
        rv.setAdapter( adapter );

        // 追加ボタン
        FloatingActionButton fab = findViewById(R.id.add_fab);
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle( "食材の追加" );

                // 食材名入力欄
                foodName = new EditText( view.getContext() );
                foodName.setHint("食材名を入力");

                // 賞味期限入力欄（カレンダー起動）
                expiration = new EditText( view.getContext() );
                expiration.setHint("賞味期限を入力");
                expiration.setOnClickListener( new View.OnClickListener(){
                    @Override
                    public void onClick( View view) {
                        //Calendarインスタンスを取得
                        final Calendar date = Calendar.getInstance();

                        //DatePickerDialogインスタンスを取得
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                view.getContext(),
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet( DatePicker view, int year, int month, int dayOfMonth) {
                                        //setした日付を取得して表示
                                        expiration.setText(String.format("%d/%02d/%02d", year, month+1, dayOfMonth));
                                    }
                                },
                                date.get(Calendar.YEAR),
                                date.get(Calendar.MONTH),
                                date.get(Calendar.DATE)
                        );
                        datePickerDialog.show();
                    }
                });

                LinearLayout layout = new LinearLayout(view.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(foodName);
                layout.addView(expiration);
                builder.setView(layout);

                builder.setMessage("賞味期限を入力してください。");
                builder.setPositiveButton("登録", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // DBに登録
                                try {
                                SpannableStringBuilder sb = (SpannableStringBuilder) foodName.getText();
                                String inputFoodName = sb.toString();

                                sb = (SpannableStringBuilder) expiration.getText();
                                String inputExpiration = sb.toString();
                                SimpleDateFormat sdf = new SimpleDateFormat();
                                sdf.applyPattern( "yyyy/MM/dd" );
                                Date date = sdf.parse( inputExpiration );
                                long unixtime = date.getTime() / 1000;

                                SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
                                ContentValues cv = new ContentValues();
                                cv.put(FoodExpirationContract.FoodExpiration.COLUMN_NAME_TITLE, inputFoodName);
                                cv.put(FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION, unixtime);
                                db.insert( FoodExpirationContract.FoodExpiration.TABLE_NAME, null, cv );
                                FoodData addedFoodData = new FoodData();
                                addedFoodData.setFoodName( inputFoodName );
                                addedFoodData.setExpiration( date );
                                foodList.add( addedFoodData );
                                adapter.notifyDataSetChanged();

                                dialog.dismiss();
                                } catch (Exception e) {
                                    // do nothing
                                }
                            }
                        });
                builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    /*
     * DBから食材一覧を賞味期限の近いものから取得する
     * @return 食材一覧
     */
    private List<FoodData> getFoodList() {

        dbOpenHelper = new DBOpenHelper(getApplicationContext());
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String[] columns = {
                FoodExpirationContract.FoodExpiration._ID,
                FoodExpirationContract.FoodExpiration.COLUMN_NAME_TITLE,
                FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION
        };
        String sortOrder = FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION + " DESC";
        Cursor cursor = db.query( FoodExpirationContract.FoodExpiration.TABLE_NAME, columns,
                null, null, null, null, sortOrder );

        List<FoodData> foodList = new ArrayList<>( );
        while(cursor.moveToNext()) {
            FoodData foodData = new FoodData();
            foodData.setFoodName( cursor.getString( cursor.getColumnIndexOrThrow( FoodExpirationContract.FoodExpiration.COLUMN_NAME_TITLE )));
            foodData.setExpiration(
                    new Date(cursor.getLong(
                            cursor.getColumnIndexOrThrow( FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION )) * 1000));
            foodList.add( foodData );
        }
        return foodList;
    }
}