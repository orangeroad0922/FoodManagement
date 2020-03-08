package com.example.foodmanagement;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.foodmanagement.model.FoodData;

import java.util.Date;
import java.util.List;

public class ExpirationNotifyWorker extends Worker {

    private static final String CHANNEL_ID_EXPIRATION = "expire";

    public ExpirationNotifyWorker( @NonNull Context context,
                                   @NonNull WorkerParameters params ) {
        super( context, params );
    }

    @Override
    public Result doWork() {

        Context context = getApplicationContext();
        Log.d( "-----", "check expiration task called." );
        FoodService svc = new FoodService();
        List<FoodData> foodList = svc.getFoodList( context );
        Date now = new Date();
        foodList.forEach( ex -> {
            long nowMills = now.getTime();
            long expirationMills = ex.getExpiration().getTime();

            long diff =
                    ( expirationMills - nowMills ) / 1000 * 60 * 60 * 24;

            // 賞味期限切れ3日前に通知
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder( getApplicationContext(), CHANNEL_ID_EXPIRATION )
                            .setSmallIcon( R.drawable.kuma )
                            .setContentTitle( "賞味期限のお知らせ" )
                            .setPriority( NotificationCompat.PRIORITY_DEFAULT );

            // 賞味期限1日前の通知
            if (diff <= 1) {
                Log.d( "通知：", ex.getFoodName() + "はあと1日で賞味期限が切れます" );
                builder.setContentText( ex.getFoodName() + "はあと1日で賞味期限が切れます" );
                NotificationManagerCompat.from( context ).notify( 123,
                        builder.build() );

                // 賞味期限3日前の通知
            } else if (diff <= 3) {
                Log.d( "通知：", ex.getFoodName() + "はあと3日で賞味期限が切れます" );
                builder.setContentText( ex.getFoodName() + "はあと3日で賞味期限が切れます" );
                NotificationManagerCompat.from( context ).notify( 123,
                        builder.build() );
            }
        } );

        return Result.success();
    }
}
