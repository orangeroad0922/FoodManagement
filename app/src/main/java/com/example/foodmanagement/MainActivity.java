package com.example.foodmanagement;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.foodmanagement.adapter.foodListRecycleViewAdapter;
import com.example.foodmanagement.database.DBOpenHelper;
import com.example.foodmanagement.database.FoodExpirationContract;
import com.example.foodmanagement.model.FoodData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText expiration;
    private EditText foodName;
    private List<FoodData> foodList;
    private foodListRecycleViewAdapter adapter;
    private static final String CHANNEL_ID_EXPIRATION = "expire";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // 通知チャンネル設定
        createNotificationChannel();

        // DBから一覧を取得
        FoodService svc = new FoodService();
        foodList = svc.getFoodList( this );

        // 一覧ビューの生成
        RecyclerView rv = findViewById( R.id.food_list_view );
        adapter = new foodListRecycleViewAdapter( foodList );

        LinearLayoutManager llm = new LinearLayoutManager( this );
        rv.setHasFixedSize( true );
        rv.setLayoutManager( llm );
        rv.setAdapter( adapter );

        // スワイプによる食材削除
        swipeListItem( rv );

        // FABによる食材追加
        fab();

        // 監視ジョブの起動
        Constraints constraints = new Constraints.Builder().setRequiresCharging( true ).build();
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder( ExpirationNotifyWorker.class
                        , 1, TimeUnit.MINUTES )
                        .setConstraints( constraints ).build();
        WorkManager.getInstance( this ).enqueue( saveRequest );

    }

    /*
     * 通知チャンネル設定
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel( CHANNEL_ID_EXPIRATION, "消費期限のお知らせ",
                            importance );
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService( NotificationManager.class );
            notificationManager.createNotificationChannel( channel );
        }
    }

    /*
     * 一覧上の食材のスワイプ操作
     */
    private void swipeListItem( RecyclerView rv ) {

        DBOpenHelper dbOpenHelper = new DBOpenHelper( getApplicationContext() );
        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback( ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT ) {

                    @Override
                    public boolean onMove( @NonNull RecyclerView recyclerView,
                                           @NonNull RecyclerView.ViewHolder viewHolder,
                                           @NonNull RecyclerView.ViewHolder target ) {

                        final int fromPos = viewHolder.getAdapterPosition();
                        final int toPos = target.getAdapterPosition();
                        adapter.notifyItemMoved( fromPos, toPos );
                        return true;// true if moved, false otherwise
                    }

                    @Override
                    public void onSwiped( @NonNull RecyclerView.ViewHolder viewHolder, int direction ) {

                        final int fromPos = viewHolder.getAdapterPosition();
                        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
                        FoodData targetFoodData = foodList.get( fromPos );

                        ContentValues cv = new ContentValues();
                        db.delete( FoodExpirationContract.FoodExpiration.TABLE_NAME,
                                FoodExpirationContract.FoodExpiration.COLUMN_NAME_ID + " = ?",
                                new String[]{ String.valueOf( targetFoodData.getId() ) } );
                        foodList.remove( fromPos );
                        adapter.notifyItemRemoved( fromPos );
                    }
                } );

        mIth.attachToRecyclerView( rv );
    }

    /*
     * 食材の追加用ボタン操作
     */
    private void fab() {

        Context context = MainActivity.this;
        DBOpenHelper dbOpenHelper = new DBOpenHelper( context );
        FloatingActionButton fab = findViewById( R.id.add_fab );

        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder( context );
                builder.setTitle( "食材の追加" );

                // 食材名入力欄
                foodName = new EditText( context );
                foodName.setHint( "食材名を入力" );

                // 賞味期限入力欄（カレンダー起動）
                expiration = new EditText( context );
                expiration.setHint( "賞味期限を入力" );
                expiration.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View view ) {

                        //Calendarインスタンスを取得
                        final Calendar date = Calendar.getInstance();

                        //DatePickerDialogインスタンスを取得
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                context,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet( DatePicker view, int year, int month, int dayOfMonth ) {
                                        //setした日付を取得して表示
                                        expiration.setText( String.format( "%d/%02d/%02d", year, month + 1, dayOfMonth ) );
                                    }
                                },
                                date.get( Calendar.YEAR ),
                                date.get( Calendar.MONTH ),
                                date.get( Calendar.DATE )
                        );
                        datePickerDialog.show();
                    }
                } );

                LinearLayout layout = new LinearLayout( context );
                layout.setOrientation( LinearLayout.VERTICAL );
                layout.addView( foodName );
                layout.addView( expiration );
                builder.setView( layout );

                builder.setMessage( "賞味期限を入力してください。" );
                builder.setPositiveButton( "登録", new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int id ) {
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
                            cv.put( FoodExpirationContract.FoodExpiration.COLUMN_NAME_FOODNAME, inputFoodName );
                            cv.put( FoodExpirationContract.FoodExpiration.COLUMN_NAME_EXPIRATION, unixtime );
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
                } );
                builder.setNegativeButton( "キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int id ) {
                        // User cancelled the dialog
                    }
                } );

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } );
    }
}